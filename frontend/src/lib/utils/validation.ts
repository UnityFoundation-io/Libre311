import { z, ZodError } from 'zod';
import libphonenumber from 'google-libphonenumber';

export type UnvalidatedInput<T> = {
	type: 'unvalidated';
	value?: T;
	error: undefined;
};

export type ValidInput<T> = {
	type: 'valid';
	value: T;
	error: undefined;
};

export type InvalidInput<T> = {
	type: 'invalid';
	value?: T;
	error: string;
};

export type ValidatedInput<T> = ValidInput<T> | InvalidInput<T>;

export type FormInputValue<T> = UnvalidatedInput<T> | ValidatedInput<T>;

export type InputValidator<T> = (value: FormInputValue<T>) => ValidatedInput<T>;

export function createInput<T>(startingValue: T | undefined = undefined): FormInputValue<T> {
	return {
		type: 'unvalidated',
		value: startingValue,
		error: undefined
	};
}

export function inputValidatorFactory<T>(schema: z.ZodType<T, z.ZodTypeDef, T>): InputValidator<T> {
	const validator: InputValidator<T> = (input: FormInputValue<T>): ValidatedInput<T> => {
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

export const numberValidator: InputValidator<number> = inputValidatorFactory(z.number());

export const stringValidator: InputValidator<string> = inputValidatorFactory(z.string());

export const optionalStringValidator: InputValidator<string | undefined> = inputValidatorFactory(
	z.string().optional()
);
export const urlValidator: InputValidator<string> = inputValidatorFactory(z.string().url());
export const emailValidator: InputValidator<string> = inputValidatorFactory(z.string().email());
export const optionalEmailValidator = inputValidatorFactory(z.string().email().optional());
// if we need to allow valid emails, empty strings, and undefined
// // https://github.com/colinhacks/zod/issues/2513#issuecomment-1732405993
export const optionalCoalesceEmailValidator = inputValidatorFactory(
	z.union([z.literal(''), z.string().email().optional()])
);

// allow strings, empty strings and undefined
export const optionalCoalesceStringValidator = inputValidatorFactory(
	z.union([z.literal(''), z.string().optional()])
);

// allow alphabetical characters including accents, empty strings and undefined
export const optionalCoalesceNameValidator = inputValidatorFactory(
	z.union([z.literal(''), z.string().regex(new RegExp('([a-zA-Z]|[à-ü]|[À-Ü])')).optional()])
);

// allow alphabetical characters including accents, empty strings and undefined
export const optionalCoalescePhoneNumberValidator = inputValidatorFactory(
	z.union([
		z.literal(''),
		z
			.string()
			.superRefine((val, ctx) => {
				const defaultErr = {
					code: z.ZodIssueCode.custom,
					message: 'Phone number is invalid'
				};
				try {
					const phoneUtil = libphonenumber.PhoneNumberUtil.getInstance();
					const parsed = phoneUtil.parseAndKeepRawInput(val, 'US');
					const valid = phoneUtil.isValidNumberForRegion(parsed, 'US');
					if (!valid) {
						ctx.addIssue(defaultErr);
					}
				} catch (error: unknown) {
					if (error instanceof Error) {
						ctx.addIssue({
							code: z.ZodIssueCode.custom,
							message: error.message
						});
					} else {
						ctx.addIssue(defaultErr);
					}
				}
			})
			.optional()
	])
);
