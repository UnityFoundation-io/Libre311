import type {
	AttributeResponse,
	BaseServiceDefinitionAttribute
} from '$lib/services/Libre311/Libre311';

export type AttributeSelection = {
	code: BaseServiceDefinitionAttribute['code'];
	attributeResponse: AttributeResponse[] | AttributeResponse;
};

// export type MultiSelectValue = {
//     code: MultiSelectServiceDefinitionAttribute['code'];
//     attributeResponse: AttributeResponse[];
// };
