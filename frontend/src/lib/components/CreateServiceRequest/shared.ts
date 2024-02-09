import type {
	ContactInformation,
	CreateServiceRequestParams,
	Service
} from '$lib/services/Libre311/Libre311';
import type { AttributeInputMap } from './ServiceDefinitionAttributes/shared';

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
};

export function toCreateServiceRequestParams(
	uiParams: CreateServiceRequestUIParams
): CreateServiceRequestParams {
	return {
		...uiParams,
		service_code: uiParams.service.service_code,
		attributes: Array.from(uiParams.attributeMap.values())
			.filter((attr) => Boolean(attr.value))
			.map((attrInpt) => {
				return {
					code: attrInpt.attribute.code,
					value: String(attrInpt.value)
				};
			})
	};
}
