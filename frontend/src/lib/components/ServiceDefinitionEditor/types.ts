import type { FormInputValue } from '$lib/utils/validation';

export type AttributeEditValue = { key: string; name: string };

export type AttributeEditInput = {
	code: number;
	required: boolean;
	description: FormInputValue<string>;
	dataTypeDescription: FormInputValue<string>;
	values: AttributeEditValue[] | undefined;
};
