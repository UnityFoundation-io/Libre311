import type { CreateServiceRequestUIParams } from '$lib/components/CreateServiceRequest/shared';
import type { ServiceDefinitionAttributeInputUnion } from '$lib/components/CreateServiceRequest/ServiceDefinitionAttributes/shared';

const DB_NAME = 'libre311-draft';
const STORE_NAME = 'draft';
const DB_VERSION = 1;
const DRAFT_KEY = 'current';

type StoredDraft = {
	id: typeof DRAFT_KEY;
	step: number;
	lat?: string;
	long?: string;
	address_string?: string;
	attributeMapEntries?: [string, ServiceDefinitionAttributeInputUnion][];
	description?: string;
	photoBlob?: Blob;
	service?: CreateServiceRequestUIParams['service'];
	first_name?: string;
	last_name?: string;
	phone?: string;
	email?: string;
	project_id?: number;
	project_slug?: string;
};

export type DraftState = {
	step: number;
	params: Partial<CreateServiceRequestUIParams>;
};

export interface DraftStore {
	save(step: number, params: Partial<CreateServiceRequestUIParams>): Promise<void>;
	load(): Promise<DraftState | null>;
	clear(): Promise<void>;
}

function openDB(): Promise<IDBDatabase> {
	return new Promise((resolve, reject) => {
		const request = indexedDB.open(DB_NAME, DB_VERSION);
		request.onupgradeneeded = () => {
			const db = request.result;
			if (!db.objectStoreNames.contains(STORE_NAME)) {
				db.createObjectStore(STORE_NAME, { keyPath: 'id' });
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

export function createDraftStore(): DraftStore {
	async function save(step: number, params: Partial<CreateServiceRequestUIParams>): Promise<void> {
		const draft: StoredDraft = {
			id: DRAFT_KEY,
			step,
			lat: params.lat,
			long: params.long,
			address_string: params.address_string,
			attributeMapEntries: params.attributeMap
				? Array.from(params.attributeMap.entries())
				: undefined,
			description: params.description,
			photoBlob: params.file,
			service: params.service,
			first_name: params.first_name,
			last_name: params.last_name,
			phone: params.phone,
			email: params.email,
			project_id: params.project_id,
			project_slug: params.project_slug
		};
		const db = await openDB();
		const tx = db.transaction(STORE_NAME, 'readwrite');
		tx.objectStore(STORE_NAME).put(draft);
		await txPromise(tx);
		db.close();
	}

	async function load(): Promise<DraftState | null> {
		const db = await openDB();
		const tx = db.transaction(STORE_NAME, 'readonly');
		const draft = await reqPromise<StoredDraft | undefined>(
			tx.objectStore(STORE_NAME).get(DRAFT_KEY)
		);
		db.close();

		if (!draft?.lat) return null;

		const params: Partial<CreateServiceRequestUIParams> = {
			lat: draft.lat,
			long: draft.long,
			address_string: draft.address_string
		};

		if (draft.attributeMapEntries) {
			params.attributeMap = new Map(draft.attributeMapEntries);
		}
		if (draft.description) params.description = draft.description;
		if (draft.photoBlob) {
			params.file = new File([draft.photoBlob], 'draft-photo.jpg', {
				type: draft.photoBlob.type || 'image/jpeg'
			});
		}
		if (draft.service) params.service = draft.service;
		if (draft.first_name) params.first_name = draft.first_name;
		if (draft.last_name) params.last_name = draft.last_name;
		if (draft.phone) params.phone = draft.phone;
		if (draft.email) params.email = draft.email;
		if (draft.project_id) params.project_id = draft.project_id;
		if (draft.project_slug) params.project_slug = draft.project_slug;

		return { step: draft.step, params };
	}

	async function clear(): Promise<void> {
		const db = await openDB();
		const tx = db.transaction(STORE_NAME, 'readwrite');
		tx.objectStore(STORE_NAME).delete(DRAFT_KEY);
		await txPromise(tx);
		db.close();
	}

	return { save, load, clear };
}
