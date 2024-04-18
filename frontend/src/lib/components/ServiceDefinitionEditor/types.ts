import type { FormInputValue } from '$lib/utils/validation';

export type AttributeValue = { key: string; name: string };

export type AttributeInput = {
	code: number;
	required: boolean;
	description: FormInputValue<string>;
	dataTypeDescription: FormInputValue<string>;
	values: AttributeValue[] | undefined;
};
