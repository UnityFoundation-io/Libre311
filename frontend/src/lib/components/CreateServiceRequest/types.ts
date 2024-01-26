import type { CreateServiceRequestParams } from '$lib/services/Libre311/Libre311';

export enum CreateServiceRequestSteps {
	LOCATION,
	PHOTO,
	DETAILS,
	CONTACT_INFO,
	REVIEW
}

// shared event types for CreateServiceRequest components
export type StepChangeEvent = {
	stepChange: Partial<CreateServiceRequestParams>;
};
