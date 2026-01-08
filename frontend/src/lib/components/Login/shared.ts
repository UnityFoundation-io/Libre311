import type { EventDispatcher } from 'svelte';

export type EventDispatchTypeMap = {
	inputChange: {
		type: 'email' | 'password';
		value: string | undefined;
	};
	login: undefined;
	cancel: undefined;
};

function dispatchInputChangeFactory(dispatch: EventDispatcher<EventDispatchTypeMap>) {
	return function onChange(e: Event, type: 'email' | 'password') {
		const target = e.target as HTMLInputElement;
		dispatch('inputChange', { type, value: target.value });
	};
}

export function dispatchEventFunctionFactory(dispatch: EventDispatcher<EventDispatchTypeMap>) {
	return {
		onChange: dispatchInputChangeFactory(dispatch),
		onSubmit: () => dispatch('login'),
		onCancel: () => dispatch('cancel')
	};
}
