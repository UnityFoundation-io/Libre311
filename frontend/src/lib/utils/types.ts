import type { FormInputValue } from "./validation";

export type Maybe<T> = T | undefined | null;
export type HasId<T> = {
	id: T;
};

export type Login = {
	emailInput: FormInputValue<string | undefined>;
	passwordInput: FormInputValue<string | undefined>;
};
