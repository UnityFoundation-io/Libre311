import type { EventDispatcher } from "svelte"

export type EventDispatchTypeMap = {
	inputChange: {
		type: 'email' |'password',
		value: string | undefined
	},
	login: undefined
}

function dispatchInputChangeFactory (dispatch: EventDispatcher<EventDispatchTypeMap>) {
	return function onChange(e: Event, type: 'email' | 'password') {
		const target = e.target as HTMLInputElement;
		dispatch('inputChange', { type, value: target.value });
	}
}

function dispatchLoginEventFactory (dispatch: EventDispatcher<EventDispatchTypeMap>) {
	return () => dispatch('login');
}

export function dispatchEventFunctionFactory (dispatch: EventDispatcher<EventDispatchTypeMap>) {
	return {
		onChange: dispatchInputChangeFactory(dispatch),
		onSubmit: dispatchLoginEventFactory(dispatch)
	}
}
