import { describe, expect, it } from 'vitest';
import { createEventBus, BaseObservable } from './EventBus';

type SampleAccount = {
	username: string;
	age: number;
};

type MockAuthEvents = {
	login: SampleAccount;
	logout: void;
};

function useAccountState() {
	let accountState: SampleAccount | undefined;

	function setAccountState(update: SampleAccount | undefined) {
		accountState = update;
	}

	function getAccountState() {
		return accountState;
	}
	return { getAccountState, setAccountState };
}

const sampleUser: SampleAccount = { username: 'Josh', age: 24 };
const sampleUser2: SampleAccount = { username: 'Jack', age: 29 };

describe('Test EventBus pub/sub actions', () => {
	const authEventBus = createEventBus<MockAuthEvents>();

	it('Single subscriber is notified of event change', () => {
		const { getAccountState, setAccountState } = useAccountState();

		authEventBus.subscribe('login', setAccountState);
		authEventBus.publish('login', sampleUser);

		expect(getAccountState()).toBe(sampleUser);
	});

	it('Works with multiple subscriptions', () => {
		const { getAccountState, setAccountState } = useAccountState();

		authEventBus.subscribe('login', setAccountState);
		authEventBus.publish('login', sampleUser);

		authEventBus.subscribe('logout', () => setAccountState(undefined));
		authEventBus.publish('logout', undefined);

		expect(getAccountState()).toBeUndefined();
	});

	it('Can successfully unsubscribe', () => {
		const { getAccountState, setAccountState } = useAccountState();

		const unsubscribe = authEventBus.subscribe('login', setAccountState);
		unsubscribe();
		authEventBus.publish('login', sampleUser);

		expect(getAccountState()).toBeUndefined();
	});

	it('Single subscriber is notified of multiple event changes', () => {
		const { getAccountState, setAccountState } = useAccountState();

		// Events
		authEventBus.subscribe('login', setAccountState);
		authEventBus.subscribe('logout', () => {
			setAccountState(undefined);
		});

		// Login
		authEventBus.publish('login', sampleUser);
		expect(getAccountState()).toBe(sampleUser);

		// Logout
		authEventBus.publish('logout', void 0);
		expect(getAccountState()).toBe(undefined);

		// Log back in as another account
		authEventBus.publish('login', sampleUser2);
		expect(getAccountState()).toBe(sampleUser2);
	});
});

describe('Can inherit from BaseObservable class', () => {
	class MockAuthSdk extends BaseObservable<MockAuthEvents> {
		constructor() {
			super();
		}

		login() {
			console.log('Logging in...');
			this.publish('login', sampleUser);
		}

		logout() {
			console.log('Logging out...');
			this.publish('logout', undefined);
		}
	}

	const mockAuthSdk = new MockAuthSdk();

	it('Clients of the MockAuthSdk can subscribe to MockAuthSdk events', () => {
		const { getAccountState, setAccountState } = useAccountState();

		mockAuthSdk.subscribe('login', setAccountState);
		mockAuthSdk.login();

		expect(getAccountState()).toBe(sampleUser);
	});
});
