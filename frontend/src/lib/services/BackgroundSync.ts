import type { Libre311Service } from './Libre311/Libre311';
import type { OfflineQueue, PendingRequest } from './OfflineQueue';
import type { Libre311Alert } from '$lib/context/Libre311AlertStore';

const MAX_ATTEMPTS = 3;

export interface BackgroundSync {
	syncAll(): Promise<void>;
}

export function createBackgroundSync(
	libre311Service: Libre311Service,
	offlineQueue: OfflineQueue,
	alertStore: Libre311Alert,
	onSyncComplete?: () => void
): BackgroundSync {
	let syncing = false;

	async function syncOne(entry: PendingRequest): Promise<boolean> {
		try {
			let mediaUrl: string | undefined = undefined;

			// Upload photo if present
			if (entry.photoBlob) {
				const file = new File([entry.photoBlob], 'offline-photo.jpg', {
					type: entry.photoBlob.type || 'image/jpeg'
				});
				mediaUrl = await libre311Service.uploadImage(file);
			}

			// Clear offline placeholder media_url if present
			const existingMediaUrl = entry.params.media_url;
			const finalMediaUrl =
				mediaUrl ?? (existingMediaUrl === 'offline://pending' ? undefined : existingMediaUrl);

			const params = {
				...entry.params,
				client_request_id: entry.clientRequestId,
				media_url: finalMediaUrl
			};

			await libre311Service.createServiceRequest(params);
			await offlineQueue.remove(entry.clientRequestId);
			return true;
		} catch (error) {
			console.error(`Failed to sync request ${entry.clientRequestId}:`, error);
			await offlineQueue.incrementAttempts(entry.clientRequestId);
			return false;
		}
	}

	async function syncAll(): Promise<void> {
		if (syncing) return;
		syncing = true;

		try {
			const entries = await offlineQueue.getAll();
			if (entries.length === 0) {
				syncing = false;
				return;
			}

			let successCount = 0;
			let failCount = 0;

			for (const entry of entries) {
				if (entry.attempts >= MAX_ATTEMPTS) {
					failCount++;
					continue;
				}

				const success = await syncOne(entry);
				if (success) {
					successCount++;
				} else {
					failCount++;
				}
			}

			if (successCount > 0) {
				alertStore.alert({
					type: 'success',
					title: 'Sync Complete',
					description: `${successCount} queued request${successCount > 1 ? 's' : ''} submitted successfully.`
				});
				onSyncComplete?.();
			}

			if (failCount > 0) {
				alertStore.alert({
					type: 'warn',
					title: 'Sync Issues',
					description: `${failCount} request${failCount > 1 ? 's' : ''} could not be synced. They will be retried.`
				});
			}
		} finally {
			syncing = false;
		}
	}

	return { syncAll };
}
