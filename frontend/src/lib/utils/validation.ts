import { z, ZodError } from 'zod';

export type UnvalidatedInput = {
	type: 'unvalidated';
	value?: unknown;
	error: undefined;
};

export type ValidInput<T> = {
	type: 'valid';
	value: T;
	error: undefined;
};

export type InvalidInput = {
	type: 'invalid';
	value: unknown;
	error: string;
};

export type ValidatedInput<T> = ValidInput<T> | InvalidInput;

export type FormInputValue<T> = UnvalidatedInput | ValidatedInput<T>;

export type InputValidator<T> = (value: UnvalidatedInput) => ValidatedInput<T>;

export function createUnvalidatedInput(startingValue: unknown = undefined): UnvalidatedInput {
	return {
		type: 'unvalidated',
		value: startingValue,
		error: undefined
	};
}

export function inputValidatorFactory<T>(schema: z.ZodType<T, z.ZodTypeDef, T>): InputValidator<T> {
	const validator: InputValidator<T> = (input: UnvalidatedInput): ValidatedInput<T> => {
		try {
			const parsedValue = schema.parse(input.value);
			return { error: undefined, type: 'valid', value: parsedValue };
		} catch (error) {
			const zodError = error as ZodError;
			const firstIssue = zodError.errors[0];
			return {
				type: 'invalid',
				value: input.value,
				error: firstIssue?.message ?? zodError.message
			};
		}
	};

	return validator;
}

export const urlValidator: InputValidator<string> = inputValidatorFactory(z.string().url());
export const emailValidator: InputValidator<string> = inputValidatorFactory(z.string().email());
export const optionalEmailValidator = inputValidatorFactory(z.string().email().nullish());
// if we need to allow empty strings and nullish values
// // https://github.com/colinhacks/zod/issues/2513#issuecomment-1732405993
// export const nullishCoalesceEmailValidator = inputValidatorFactory(
// 	z.union([z.literal(''), z.string().email().nullish()])
// );
