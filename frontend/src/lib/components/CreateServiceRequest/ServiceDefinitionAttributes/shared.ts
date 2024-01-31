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

type ServiceDefinitionAttributeLookupMap = {
	multivaluelist: MultiSelectServiceDefinitionAttribute;
	singlevaluelist: SingleValueListServiceDefinitionAttribute;
	string: StringServiceDefinitionAttribute;
	number: NumberServiceDefinitionAttribute;
	datetime: DateTimeServiceDefinitionAttribute;
	text: TextServiceDefinitionAttribute;
};

export type ServiceDefinitionAttributeInput<
	T extends keyof ServiceDefinitionAttributeLookupMap,
	K
> = {
	datatype: T;
	error?: string; // an error if the attribute value is not valid (ie a value is required and no value was provided)
	attribute: ServiceDefinitionAttributeLookupMap[T]; // a reference to the corresponding attribute
	value?: K; // the actual form input value
};

export type MultiSelectServiceDefinitionAttributeInput = ServiceDefinitionAttributeInput<
	'multivaluelist',
	string[]
>;

export type SingleValueListServiceDefinitionAttributeInput = ServiceDefinitionAttributeInput<
	'singlevaluelist',
	string
>;

export type StringServiceDefinitionInput = ServiceDefinitionAttributeInput<'string', string>;

export type NumberServiceDefinitionInput = ServiceDefinitionAttributeInput<'number', number>;

export type DateTimeServiceDefinitionAttributeInput = ServiceDefinitionAttributeInput<
	'datetime',
	Date
>;

export type TextServiceDefinitionAttributeInput = ServiceDefinitionAttributeInput<'text', string>;

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
				attributeInputMap.set(attribute.code, createInputProps('multivaluelist', attribute, []));
				break;
			case 'singlevaluelist':
				attributeInputMap.set(
					attribute.code,
					createInputProps('singlevaluelist', attribute, undefined)
				);
				break;
			case 'datetime':
				attributeInputMap.set(attribute.code, createInputProps('datetime', attribute, undefined));
				break;
			case 'number':
				attributeInputMap.set(attribute.code, createInputProps('number', attribute, undefined));
				break;
			case 'string':
				attributeInputMap.set(attribute.code, createInputProps('string', attribute, undefined));
				break;
			case 'text':
				attributeInputMap.set(attribute.code, createInputProps('text', attribute, undefined));
		}
	}
	return attributeInputMap;
}

export function createInputProps<T extends keyof ServiceDefinitionAttributeLookupMap, K>(
	datatype: T,
	attribute: ServiceDefinitionAttributeLookupMap[T],
	startingVal: K
): ServiceDefinitionAttributeInput<T, K> {
	return {
		datatype: datatype,
		attribute,
		value: startingVal
	};
}
