let idx = 0;
const uuid = () => idx++;

type Unsubscribe = () => void;

// I used this https://github.com/sveltejs/svelte/pull/5260 as a reference for creating this type
export type EventBus<EventMap> = Publish<EventMap> & Subscribe<EventMap>;

type Publish<EventMap> = {
	publish<EventKey extends Extract<keyof EventMap, string>>(
		event: EventKey,
		args: EventMap[EventKey]
	): void;
};

export type Subscribe<EventMap> = {
	subscribe<EventKey extends Extract<keyof EventMap, string>>(
		event: EventKey,
		cb: (args: EventMap[EventKey]) => void
	): Unsubscribe;
};

export function createEventBus<T>(): EventBus<T> {
	// eslint-disable-next-line @typescript-eslint/no-explicit-any
	const subscriptions: Record<string, Record<string, (arg: any) => void>> = {};

	return {
		subscribe: (event, cb) => {
			const id = uuid();

			if (!subscriptions[event]) subscriptions[event] = {};
			subscriptions[event][id] = cb;

			return () => {
				delete subscriptions[event][id];
				if (Object.keys(subscriptions[event]).length === 0) delete subscriptions[event];
			};
		},
		publish: (event, args) => {
			if (!subscriptions[event]) return;

			Object.keys(subscriptions[event]).forEach((key) => {
				const cb = subscriptions[event][key];
				cb(args);
			});
		}
	};
}

export class BaseObservable<T> implements Subscribe<T> {
	private eventBus: EventBus<T> = createEventBus<T>();

	subscribe<EventKey extends Extract<keyof T, string>>(
		event: EventKey,
		cb: (args: T[EventKey]) => void
	): Unsubscribe {
		return this.eventBus.subscribe(event, cb);
	}
	protected publish<EventKey extends Extract<keyof T, string>>(
		event: EventKey,
		args: T[EventKey]
	): void {
		this.eventBus.publish(event, args);
	}
}
