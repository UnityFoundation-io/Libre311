import type { CreateServiceRequestParams } from './Libre311/Libre311';

export type PendingRequest = {
	clientRequestId: string;
	params: CreateServiceRequestParams;
	photoBlob?: Blob;
	serviceName: string;
	enqueuedAt: number;
	attempts: number;
};

const DB_NAME = 'libre311-offline';
const STORE_NAME = 'pending_requests';
const DB_VERSION = 1;

export interface OfflineQueue {
	enqueue(
		params: CreateServiceRequestParams,
		serviceName: string,
		photo?: Blob
	): Promise<string>;
	getAll(): Promise<PendingRequest[]>;
	remove(clientRequestId: string): Promise<void>;
	incrementAttempts(clientRequestId: string): Promise<void>;
	count(): Promise<number>;
}

function openDB(): Promise<IDBDatabase> {
	return new Promise((resolve, reject) => {
		const request = indexedDB.open(DB_NAME, DB_VERSION);

		request.onupgradeneeded = () => {
			const db = request.result;
			if (!db.objectStoreNames.contains(STORE_NAME)) {
				db.createObjectStore(STORE_NAME, { keyPath: 'clientRequestId' });
			}
		};

		request.onsuccess = () => resolve(request.result);
		request.onerror = () => reject(request.error);
	});
}

function txPromise(tx: IDBTransaction): Promise<void> {
	return new Promise((resolve, reject) => {
		tx.oncomplete = () => resolve();
		tx.onerror = () => reject(tx.error);
	});
}

function reqPromise<T>(req: IDBRequest<T>): Promise<T> {
	return new Promise((resolve, reject) => {
		req.onsuccess = () => resolve(req.result);
		req.onerror = () => reject(req.error);
	});
}

export function createOfflineQueue(): OfflineQueue {
	async function enqueue(
		params: CreateServiceRequestParams,
		serviceName: string,
		photo?: Blob
	): Promise<string> {
		const clientRequestId = crypto.randomUUID();
		const entry: PendingRequest = {
			clientRequestId,
			params,
			photoBlob: photo,
			serviceName,
			enqueuedAt: Date.now(),
			attempts: 0
		};
		const db = await openDB();
		const tx = db.transaction(STORE_NAME, 'readwrite');
		tx.objectStore(STORE_NAME).put(entry);
		await txPromise(tx);
		db.close();
		return clientRequestId;
	}

	async function getAll(): Promise<PendingRequest[]> {
		const db = await openDB();
		const tx = db.transaction(STORE_NAME, 'readonly');
		const result = await reqPromise(tx.objectStore(STORE_NAME).getAll());
		db.close();
		return result;
	}

	async function remove(clientRequestId: string): Promise<void> {
		const db = await openDB();
		const tx = db.transaction(STORE_NAME, 'readwrite');
		tx.objectStore(STORE_NAME).delete(clientRequestId);
		await txPromise(tx);
		db.close();
	}

	async function incrementAttempts(clientRequestId: string): Promise<void> {
		const db = await openDB();
		const tx = db.transaction(STORE_NAME, 'readwrite');
		const store = tx.objectStore(STORE_NAME);
		const entry = await reqPromise(store.get(clientRequestId));
		if (entry) {
			entry.attempts += 1;
			store.put(entry);
		}
		await txPromise(tx);
		db.close();
	}

	async function count(): Promise<number> {
		const db = await openDB();
		const tx = db.transaction(STORE_NAME, 'readonly');
		const result = await reqPromise(tx.objectStore(STORE_NAME).count());
		db.close();
		return result;
	}

	return { enqueue, getAll, remove, incrementAttempts, count };
}
