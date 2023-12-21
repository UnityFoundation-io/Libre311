import {
	Libre311ServiceImpl,
	type Libre311Service,
	type Libre311ServiceProps
} from '$lib/services/Libre311/Libre311';
import { getContext, setContext } from 'svelte';

const libre311CtxKey = Symbol();

//  todo hydration data here (Name of tenant)
type Libre311Context = {
	service: Libre311Service;
};

export type Libre311ContextProps = {
	service: Libre311ServiceProps;
};

export function createLibre311Context(params: Libre311ContextProps) {
	const service = new Libre311ServiceImpl(params.service);
	const libre311Context: Libre311Context = {
		service
	};
	setContext(libre311CtxKey, libre311Context);
}

export function useLibre311Service(): Libre311Service {
	return getContext<Libre311Context>(libre311CtxKey).service;
}
