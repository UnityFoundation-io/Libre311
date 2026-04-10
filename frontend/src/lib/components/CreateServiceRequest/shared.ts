import type {
	ContactInformation,
	CreateServiceRequestParams,
	Service
} from '$lib/services/Libre311/Libre311';
import type {
	AttributeInputMap,
	ServiceDefinitionAttributeInputUnion
} from './ServiceDefinitionAttributes/shared';

export enum CreateServiceRequestSteps {
	LOCATION,
	PHOTO,
	DETAILS,
	CONTACT_INFO,
	REVIEW
}

// shared event types for CreateServiceRequest components
export type StepChangeEvent = {
	stepChange: Partial<CreateServiceRequestUIParams>;
};

export type CreateServiceRequestUIParams = ContactInformation & {
	lat: string;
	long: string;
	address_string: string;
	attributeMap: AttributeInputMap;
	description?: string;
	media_url?: string;
	service: Service;
	file?: File;
	project_id?: number;
	project_slug?: string;
};

function isEmptyArrayOrUndefined(thing: string | number | Date | string[] | undefined) {
	return (Array.isArray(thing) && thing.length == 0) || thing == undefined;
}

function buildSnapshotValues(
	attrInpt: ServiceDefinitionAttributeInputUnion
): Array<{ key: string; name: string }> {
	if (
		(attrInpt.datatype === 'singlevaluelist' || attrInpt.datatype === 'multivaluelist') &&
		'values' in attrInpt.attribute
	) {
		const selectedKeys =
			attrInpt.datatype === 'multivaluelist'
				? (attrInpt.value as string[])
				: [attrInpt.value as string];
		return selectedKeys
			.filter((k): k is string => k != null)
			.map((k) => ({
				key: k,
				name: attrInpt.attribute.values.find((v) => v.key === k)?.name ?? k
			}));
	}
	return [{ key: String(attrInpt.attribute.code), name: String(attrInpt.value) }];
}

export function toCreateServiceRequestParams(
	uiParams: CreateServiceRequestUIParams
): CreateServiceRequestParams {
	const filledAttrs = Array.from(uiParams.attributeMap.values()).filter(
		(attr) => !isEmptyArrayOrUndefined(attr.value)
	);

	const attribute_snapshot = JSON.stringify(
		filledAttrs.map((attrInpt) => ({
			code: attrInpt.attribute.code,
			variable: attrInpt.attribute.variable,
			datatype: attrInpt.attribute.datatype,
			required: attrInpt.attribute.required,
			description: attrInpt.attribute.description,
			order: attrInpt.attribute.order,
			datatype_description: attrInpt.attribute.datatype_description ?? null,
			values: buildSnapshotValues(attrInpt)
		}))
	);

	return {
		...uiParams,
		service_code: uiParams.service.service_code,
		attribute_snapshot,
		attributes: filledAttrs.map((attrInpt) => ({
			code: attrInpt.attribute.code,
			value: String(attrInpt.value)
		}))
	};
}
