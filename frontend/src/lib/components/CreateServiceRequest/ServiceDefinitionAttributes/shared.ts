import type {
	CreateServiceRequestParams,
	DateTimeServiceDefinitionAttribute,
	MultiSelectServiceDefinitionAttribute,
	NumberServiceDefinitionAttribute,
	ServiceDefinition,
	ServiceDefinitionAttribute,
	SingleValueListServiceDefinitionAttribute,
	StringServiceDefinitionAttribute,
	TextServiceDefinitionAttribute
} from '$lib/services/Libre311/Libre311';

export type ServiceDefinitionAttributeInput<T extends ServiceDefinitionAttribute, K> = {
	error?: string; // an error if the attribute value is not valid (ie a value is required and no value was provided)
	attribute: T; // a reference to the corresponding attribute
	value?: K; // the actual form input value
};

export type MultiSelectServiceDefinitionAttributeInput = ServiceDefinitionAttributeInput<
	MultiSelectServiceDefinitionAttribute,
	string[]
>;

export function isMultiSelectServiceDefinitionAttributeInput(
	maybeMultiselect: ServiceDefinitionAttributeInputUnion
): maybeMultiselect is MultiSelectServiceDefinitionAttributeInput {
	return maybeMultiselect.attribute.datatype === 'multivaluelist';
}

export type SingleValueListServiceDefinitionAttributeInput = ServiceDefinitionAttributeInput<
	SingleValueListServiceDefinitionAttribute,
	string
>;

export function isSingleValueListServiceDefinitionAttributeInput(
	maybeMultiselect: ServiceDefinitionAttributeInputUnion
): maybeMultiselect is SingleValueListServiceDefinitionAttributeInput {
	return maybeMultiselect.attribute.datatype === 'singlevaluelist';
}

export type StringServiceDefinitionInput = ServiceDefinitionAttributeInput<
	StringServiceDefinitionAttribute,
	string
>;

export function isStringServiceDefinitionInput(
	maybeString: ServiceDefinitionAttributeInputUnion
): maybeString is StringServiceDefinitionInput {
	return maybeString.attribute.datatype === 'string';
}

export type NumberServiceDefinitionInput = ServiceDefinitionAttributeInput<
	NumberServiceDefinitionAttribute,
	number
>;

export function isNumberServiceDefinitionInput(
	maybeNumber: ServiceDefinitionAttributeInputUnion
): maybeNumber is NumberServiceDefinitionInput {
	return maybeNumber.attribute.datatype === 'number';
}

export type DateTimeServiceDefinitionAttributeInput = ServiceDefinitionAttributeInput<
	DateTimeServiceDefinitionAttribute,
	Date
>;

export function isDateTimeServiceDefinitionAttributeInput(
	maybeDateTime: ServiceDefinitionAttributeInputUnion
): maybeDateTime is DateTimeServiceDefinitionAttributeInput {
	return maybeDateTime.attribute.datatype === 'datetime';
}

export type TextServiceDefinitionAttributeInput = ServiceDefinitionAttributeInput<
	TextServiceDefinitionAttribute,
	string
>;

export function isTextServiceDefinitionAttributeInput(
	maybeText: ServiceDefinitionAttributeInputUnion
): maybeText is TextServiceDefinitionAttributeInput {
	return maybeText.attribute.datatype === 'text';
}

type ServiceDefinitionAttributeInputUnion =
	| MultiSelectServiceDefinitionAttributeInput
	| SingleValueListServiceDefinitionAttributeInput
	| StringServiceDefinitionInput
	| NumberServiceDefinitionInput
	| DateTimeServiceDefinitionAttributeInput
	| TextServiceDefinitionAttributeInput;

export type AttributeInputMap = Map<
	ServiceDefinitionAttribute['code'],
	ServiceDefinitionAttributeInputUnion
>;

export function createAttributeInputMap(
	serviceDefinition: ServiceDefinition,
	params: Partial<CreateServiceRequestParams>
) {
	if (params?.service?.service_code && params?.attributeMap) {
		return params.attributeMap;
	}

	const attributeInputMap: AttributeInputMap = new Map();
	for (const attribute of serviceDefinition.attributes) {
		switch (attribute.datatype) {
			case 'multivaluelist':
				attributeInputMap.set(attribute.code, createInputProps(attribute, []));
				break;
			case 'singlevaluelist':
				attributeInputMap.set(
					attribute.code,
					createInputProps<SingleValueListServiceDefinitionAttribute, string | undefined>(
						attribute,
						undefined
					)
				);
				break;
			case 'datetime':
				attributeInputMap.set(
					attribute.code,
					createInputProps<DateTimeServiceDefinitionAttribute, Date | undefined>(
						attribute,
						undefined
					)
				);
				break;
			case 'number':
				attributeInputMap.set(
					attribute.code,
					createInputProps<NumberServiceDefinitionAttribute, number | undefined>(
						attribute,
						undefined
					)
				);
				break;
			case 'string':
				attributeInputMap.set(
					attribute.code,
					createInputProps<StringServiceDefinitionAttribute, string | undefined>(
						attribute,
						undefined
					)
				);
				break;
			case 'text':
				attributeInputMap.set(
					attribute.code,
					createInputProps<TextServiceDefinitionAttribute, string | undefined>(attribute, undefined)
				);
		}
	}
	return attributeInputMap;
}

export function createInputProps<T extends ServiceDefinitionAttribute, K>(
	attribute: T,
	startingVal: K
): ServiceDefinitionAttributeInput<T, K> {
	return {
		attribute,
		value: startingVal
	};
}
