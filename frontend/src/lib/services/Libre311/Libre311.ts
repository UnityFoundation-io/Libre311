import type { AxiosInstance } from 'axios';
import axios from 'axios';
import { z } from 'zod';
import type { RecaptchaService } from '../RecaptchaService';
import type {
	UpdateSensitiveServiceRequestRequest,
	UpdateSensitiveServiceRequestResponse
} from './types/UpdateSensitiveServiceRequest';
import type { UnityAuthLoginResponse } from '../UnityAuth/UnityAuth';
import { FilteredServiceRequestsParamsMapper } from './FilteredServiceRequestsParamsMapper';

const JurisdicationIdSchema = z.string();
const HasJurisdictionIdSchema = z.object({
	jurisdiction_id: JurisdicationIdSchema
});
export type HasJurisdictionId = z.infer<typeof HasJurisdictionIdSchema>;
export type JurisdictionId = z.infer<typeof JurisdicationIdSchema>;

const RealtimeServiceTypeSchema = z.literal('realtime');
const OtherServiceTypeSchema = z.literal('other'); // todo remove once second type is found
const ServiceTypeSchema = z.union([RealtimeServiceTypeSchema, OtherServiceTypeSchema]); // todo what are the other types besides realtime?

const ServiceCodeSchema = z.number();
const HasServiceCodeSchema = z.object({
	service_code: ServiceCodeSchema
});

export const AttributeCodeSchma = z.object({
	attribute_code: z.number()
});
export type HasAttributeCode = z.infer<typeof AttributeCodeSchma>;

export const GroupSchema = z.object({ id: z.number(), name: z.string() });
export type Group = z.infer<typeof GroupSchema>;

export type HasServiceCode = z.infer<typeof HasServiceCodeSchema>;
export type ServiceCode = z.infer<typeof ServiceCodeSchema>;

const HasGroupIdSchema = z.object({
	group_id: z.number()
});

export type HasGroupId = z.infer<typeof HasGroupIdSchema>;

export const ServiceSchema = z
	.object({
		service_name: z.string(),
		description: z.string().optional(),
		metadata: z.boolean(),
		type: ServiceTypeSchema
		// keywords: z.array(z.string()),
		// group: z.string()
	})
	.merge(HasServiceCodeSchema)
	.merge(HasGroupIdSchema);

export type Service = z.infer<typeof ServiceSchema>;

const StringType = z.literal('string');
const NumberType = z.literal('number');
const DatetimeType = z.literal('datetime');
const TextType = z.literal('text');
const SingleValueListType = z.literal('singlevaluelist');
const MultiValueListType = z.literal('multivaluelist');

export const DatatypeUnionSchema = z.union([
	StringType,
	NumberType,
	DatetimeType,
	TextType,
	SingleValueListType,
	MultiValueListType
]);

export type DatatypeUnion = z.infer<typeof DatatypeUnionSchema>;

// const ServiceDefinitionAttributeCode
export const BaseServiceDefinitionAttributeSchema = z.object({
	/**
	 * true: denotes that user input is needed
	 * false: means the attribute is only used to present information to the user within the description field
	 */
	variable: z.boolean(),
	code: z.number(), // the id of the attribute in the db
	datatype: DatatypeUnionSchema,
	required: z.boolean(),
	/**
	 * A description of the datatype which helps the user provide their input (the placeholder text essentially)
	 */
	datatype_description: z.string().nullish(),
	order: z.number(),
	/**
	 * The actual question
	 */
	description: z.string()
});

export type BaseServiceDefinitionAttribute = z.infer<typeof BaseServiceDefinitionAttributeSchema>;

export const StringServiceDefinitionAttributeSchema = BaseServiceDefinitionAttributeSchema.extend({
	datatype: StringType
});

export type StringServiceDefinitionAttribute = z.infer<
	typeof StringServiceDefinitionAttributeSchema
>;

export const DateTimeServiceDefinitionAttributeSchema = BaseServiceDefinitionAttributeSchema.extend(
	{
		datatype: DatetimeType
	}
);

export type DateTimeServiceDefinitionAttribute = z.infer<
	typeof DateTimeServiceDefinitionAttributeSchema
>;

export const NumberServiceDefinitionAttributeSchema = BaseServiceDefinitionAttributeSchema.extend({
	datatype: NumberType
});

export type NumberServiceDefinitionAttribute = z.infer<
	typeof NumberServiceDefinitionAttributeSchema
>;

export const TextServiceDefinitionAttributeSchema = BaseServiceDefinitionAttributeSchema.extend({
	datatype: TextType
});

export type TextServiceDefinitionAttribute = z.infer<typeof TextServiceDefinitionAttributeSchema>;

const AttributeValueSchema = z.object({
	/**
	 * The unique identifier associated with an option for singlevaluelist or multivaluelist. This is analogous to the value attribute in an html option tag.
	 */
	key: z.string(),
	/**
	 * The human readable title of an option for singlevaluelist or multivaluelist. This is analogous to the innerhtml text node of an html option tag.
	 */
	name: z.string()
});

export type AttributeValue = z.infer<typeof AttributeValueSchema>;

export const ListBasedServiceDefinitionAttributeSchema =
	BaseServiceDefinitionAttributeSchema.extend({
		datatype: z.union([SingleValueListType, MultiValueListType]),
		values: z.array(AttributeValueSchema)
	});

export const MultiSelectServiceDefinitionAttributeSchema =
	ListBasedServiceDefinitionAttributeSchema.extend({
		datatype: MultiValueListType
	});

export const SingleValueListServiceDefinitionAttributeSchema =
	ListBasedServiceDefinitionAttributeSchema.extend({
		datatype: SingleValueListType
	});

export type MultiSelectServiceDefinitionAttribute = z.infer<
	typeof MultiSelectServiceDefinitionAttributeSchema
>;

export type SingleValueListServiceDefinitionAttribute = z.infer<
	typeof SingleValueListServiceDefinitionAttributeSchema
>;

export const ServiceDefinitionAttributeSchema = z.union([
	StringServiceDefinitionAttributeSchema,
	DateTimeServiceDefinitionAttributeSchema,
	TextServiceDefinitionAttributeSchema,
	NumberServiceDefinitionAttributeSchema,
	MultiSelectServiceDefinitionAttributeSchema,
	SingleValueListServiceDefinitionAttributeSchema
]);

export type ServiceDefinitionAttribute = z.infer<typeof ServiceDefinitionAttributeSchema>;

const ServiceDefinitionSchema = HasServiceCodeSchema.extend({
	attributes: z.array(ServiceDefinitionAttributeSchema)
});

export type ServiceDefinition = z.infer<typeof ServiceDefinitionSchema>;

const HasServiceRequestIdSchema = z.object({
	service_request_id: z.number()
});

export type HasServiceRequestId = z.infer<typeof HasServiceRequestIdSchema>;
export type ServiceRequestId = HasServiceRequestId['service_request_id'];

export const CreateServiceRequestResponseSchema = z
	.object({
		service_notice: z.string().nullish(),
		account_id: z.number().nullish()
	})
	.merge(HasServiceRequestIdSchema);

export type CreateServiceRequestResponse = z.infer<typeof CreateServiceRequestResponseSchema>;

export type InternalCreateServiceRequestResponse = z.infer<
	typeof InternalCreateServiceRequestResponseSchema
>;

const InternalCreateServiceRequestResponseSchema = z.array(CreateServiceRequestResponseSchema);

export const GetGroupListResponseSchema = z.array(GroupSchema);
export type GetGroupListResponse = z.infer<typeof GetGroupListResponseSchema>;

export const GetServiceListResponseSchema = z.array(ServiceSchema);
export type GetServiceListResponse = z.infer<typeof GetServiceListResponseSchema>;

// user response values from  ServiceDefinitionAttributeSchema.
// attribute[code1]=value1
// todo consider adding AttributeValue[name] so that ui can reflect the values later on
export type AttributeResponse = { code: ServiceDefinitionAttribute['code']; value: string };
// todo will likely need the recaptcha value here

export const EmailSchema = z.string().email();

export const ContactInformationSchema = z.object({
	first_name: z.string().optional(),
	last_name: z.string().optional(),
	phone: z.string().optional(), // todo add validation => https://www.npmjs.com/package/libphonenumber-js
	email: EmailSchema.optional()
});

export type ContactInformation = z.infer<typeof ContactInformationSchema>;

export type CreateServiceRequestParams = HasServiceCode &
	ContactInformation & {
		lat: string;
		long: string;
		address_string: string;
		attributes: AttributeResponse[];
		description?: string;
		media_url?: string;
	};

export const LowServiceRequestPrioritySchema = z.literal('low');
export const MediumServiceRequestPrioritySchema = z.literal('medium');
export const HighServiceRequestPrioritySchema = z.literal('high');
export const ServiceRequestPrioritySchema = z.union([
	LowServiceRequestPrioritySchema,
	MediumServiceRequestPrioritySchema,
	HighServiceRequestPrioritySchema
]);
export type ServiceRequestPriority = z.infer<typeof ServiceRequestPrioritySchema>;

export const serviceRequestPriorityArray: Readonly<Array<ServiceRequestPriority>> = [
	'low',
	'medium',
	'high'
];

export function isServiceRequestPriority(
	maybePriority: unknown
): maybePriority is ServiceRequestPriority {
	return ServiceRequestPrioritySchema.safeParse(maybePriority).success;
}

export const OpenServiceRequestStatusSchema = z.literal('open');
export const ClosedServiceRequestStatusSchema = z.literal('closed');
export const AssignedServiceRequestStatusSchema = z.literal('assigned');
export const InProgressServiceRequestStatusSchema = z.literal('in_progress');
export const ServiceRequestStatusSchema = z.union([
	OpenServiceRequestStatusSchema,
	ClosedServiceRequestStatusSchema,
	AssignedServiceRequestStatusSchema,
	InProgressServiceRequestStatusSchema
]);
export type ServiceRequestStatus = z.infer<typeof ServiceRequestStatusSchema>;
export const serviceRequestStatusArray: Readonly<Array<ServiceRequestStatus>> = [
	'assigned',
	'closed',
	'open',
	'in_progress'
];
export function isServiceRequestStatus(maybeStatus: unknown): maybeStatus is ServiceRequestStatus {
	return ServiceRequestStatusSchema.safeParse(maybeStatus).success;
}
const urlSchema = z.string().url();

// represents the users responses to the various service definition attributes
const SelectedValuesSchema = z.object({
	code: z.number(),
	datatype: DatatypeUnionSchema,
	description: z.string(),
	values: z.array(AttributeValueSchema) // key is the SelectOption value and name is the human readable option.  For displaying the value to users, show the name.
});
export type SelectedValue = z.infer<typeof SelectedValuesSchema>;

export const ServiceRequestSchema = z
	.object({
		status: ServiceRequestStatusSchema,
		status_notes: z.string().optional(),
		service_name: z.string(),
		description: z.string().optional(),
		agency_responsible: z.string().optional(),
		agency_email: z.string().optional(),
		service_notice: z.string().optional(),
		requested_datetime: z.string(),
		updated_datetime: z.string(),
		expected_datetime: z.string().optional(),
		address: z.string(),
		address_id: z.number().optional(),
		zipcode: z.string().optional(),
		lat: z.string(),
		long: z.string(),
		media_url: urlSchema.optional(),
		selected_values: z.array(SelectedValuesSchema).optional(),
		priority: ServiceRequestPrioritySchema.optional()
	})
	.merge(HasServiceRequestIdSchema)
	.merge(HasServiceCodeSchema)
	.merge(ContactInformationSchema);

export type ServiceRequest = z.infer<typeof ServiceRequestSchema>;

export const GetServiceRequestsResponseSchema = z.array(ServiceRequestSchema);
export type GetServiceRequestsResponse = z.infer<typeof GetServiceRequestsResponseSchema>;

// ***************** Create Group ***************** //
export const CreateGroupParamsSchema = z.object({ name: z.string() });

export type CreateGroupParams = z.infer<typeof CreateGroupParamsSchema>;

// ****************** Edit Group ****************** //

export const EditGroupParamsSchema = z.object({
	id: z.number(),
	name: z.string()
});

export type EditGroupParams = z.infer<typeof EditGroupParamsSchema>;

// ***************** Create Service *************** //

// Create Service - Request Schema
export const CreateServiceParamsSchema = z.object({
	service_name: z.string(),
	group_id: z.number()
});

//  Create Service - Response Schema
export const CreateServiceResponseSchema = z
	.object({
		jurisdiction_id: z.string(),
		group_id: z.number()
	})
	.merge(ServiceSchema);

//  Create Service - Request Type
export type CreateServiceParams = z.infer<typeof CreateServiceParamsSchema>;

// Create Service - Response Type
export type CreateServiceResponse = z.infer<typeof CreateServiceResponseSchema>;

// ***************** Edit Service *************** //

// Edit Service - Request Schema
export const EditServiceParamsSchema = z.object({
	service_code: z.number(),
	service_name: z.string()
});

// Edit Service - Request Type
export type EditServiceParams = z.infer<typeof EditServiceParamsSchema>;

// ***************** Attributes *************** //

export const CreateServiceDefinitionAttributeResponseSchema = z.object({
	service_code: z.number(),
	attributes: z.array(ServiceDefinitionAttributeSchema)
});

export type CreateServiceDefinitionAttributeResponse = z.infer<
	typeof CreateServiceDefinitionAttributeResponseSchema
>;

export const CreateServiceDefinitionAttributesSchema = z.object({
	service_code: z.number(),
	description: z.string(),
	datatype_description: z.string(),
	datatype: z.string(),
	variable: z.boolean(),
	required: z.boolean(),
	order: z.number(),
	values: z.array(AttributeValueSchema).optional()
});

export type CreateServiceDefinitionAttributesParams = z.infer<
	typeof CreateServiceDefinitionAttributesSchema
>;

export const EditServiceDefinitionAttributeParamsSchema = z.object({
	attribute_code: z.number(),
	service_code: z.number(),
	description: z.string(),
	datatype_description: z.string(),
	required: z.boolean(),
	// order: z.number(),
	values: z.array(AttributeValueSchema).optional()
});

export type EditServiceDefinitionAttributeParams = z.infer<
	typeof EditServiceDefinitionAttributeParamsSchema
>;

export const EditServiceDefinitionAttributeResponseSchema = z.object({
	service_code: z.number(),
	attributes: z.array(ServiceDefinitionAttributeSchema)
});

export type EditServiceDefinitionAttributeResponse = z.infer<
	typeof EditServiceDefinitionAttributeResponseSchema
>;

/**
 * Filter Params for retrieving a subset service requests
 */
export type FilteredServiceRequestsParams =
	| ServiceRequestId[]
	| {
			servicePriority?: ServiceRequestPriority[];
			serviceCode?: ServiceCode[] | undefined;
			startDate?: string;
			endDate?: string;
			status?: ServiceRequestStatus[];
			pageNumber?: number;
	  };

const latLngTupleSchema = z.tuple([z.number(), z.number()]);

const JurisdictionConfigSchema = z
	.object({
		name: z.string(),
		bounds: z.array(latLngTupleSchema).min(1),
		auth_base_url: z.string()
	})
	.merge(HasJurisdictionIdSchema);

export type JurisdictionConfig = z.infer<typeof JurisdictionConfigSchema>;

const PaginationSchema = z.object({
	size: z.number(), // the number of records per page
	offset: z.number(), // if pageSize = 10 and pageNumber = 5 then offset = 50,
	pageNumber: z.number(), // the current page number (first page starts at 0)
	totalPages: z.number(), // the total number of pages
	totalSize: z.number() // the total number of records
});

export type Pagination = z.infer<typeof PaginationSchema>;

export const EMPTY_PAGINATION = {
	size: 0,
	offset: 0,
	pageNumber: 0,
	totalPages: 0,
	totalSize: 0
};

export type HasPagination = {
	pagination: Pagination;
};

export type HasMetadata<T> = {
	metadata: T;
};

export type ServiceRequestsResponse = {
	serviceRequests: GetServiceRequestsResponse;
} & HasMetadata<HasPagination>;

// https://wiki.open311.org/GeoReport_v2/
export interface Open311Service {
	getServiceList(): Promise<GetServiceListResponse>;
	getServiceDefinition(params: HasServiceCode): Promise<ServiceDefinition>;
	createServiceRequest(params: CreateServiceRequestParams): Promise<CreateServiceRequestResponse>;
	getServiceRequests(params: FilteredServiceRequestsParams): Promise<ServiceRequestsResponse>;
	getServiceRequest(params: HasServiceRequestId): Promise<ServiceRequest>;
}

export const ReverseGeocodeResponseSchema = z.object({
	display_name: z.string()
});
export type ReverseGeocodeResponse = z.infer<typeof ReverseGeocodeResponseSchema>;

export const LibrePermissionsSchema = z.union([
	z.literal('AUTH_SERVICE_EDIT-SYSTEM'),
	z.literal('AUTH_SERVICE_VIEW-SYSTEM'),
	z.literal('AUTH_SERVICE_EDIT-TENANT'),
	z.literal('AUTH_SERVICE_VIEW-TENANT'),
	z.literal('LIBRE311_ADMIN_EDIT-SYSTEM'),
	z.literal('LIBRE311_ADMIN_VIEW-SYSTEM'),
	z.literal('LIBRE311_ADMIN_EDIT-TENANT'),
	z.literal('LIBRE311_ADMIN_VIEW-TENANT'),
	z.literal('LIBRE311_ADMIN_EDIT-SUBTENANT'),
	z.literal('LIBRE311_ADMIN_VIEW-SUBTENANT'),
	z.literal('LIBRE311_REQUEST_EDIT-SYSTEM'),
	z.literal('LIBRE311_REQUEST_VIEW-SYSTEM'),
	z.literal('LIBRE311_REQUEST_EDIT-TENANT'),
	z.literal('LIBRE311_REQUEST_VIEW-TENANT'),
	z.literal('LIBRE311_REQUEST_EDIT-SUBTENANT'),
	z.literal('LIBRE311_REQUEST_VIEW-SUBTENANT')
]);

export type LibrePermissions = z.infer<typeof LibrePermissionsSchema>;

type DeleteAttributeParams = {
	serviceCode: number;
	attributeCode: number;
};

type UpdateServiceOrder = { order_position: number } & HasServiceCode;
type UpdateServicesOrderParams = {
	services: Array<UpdateServiceOrder>;
} & HasGroupId;

type UpdateAttributeOrder = { code: HasAttributeCode['attribute_code']; order: number };
type UpdateAttributesOrderParams = {
	attributes: Array<UpdateAttributeOrder>;
} & HasServiceCode;
export interface Libre311Service extends Open311Service {
	getJurisdictionConfig(): JurisdictionConfig;
	reverseGeocode(coords: L.PointTuple): Promise<ReverseGeocodeResponse>;
	uploadImage(file: File): Promise<string>;
	setAuthInfo(authInfo: UnityAuthLoginResponse | undefined): void;
	getGroupList(): Promise<GetGroupListResponse>;
	createGroup(params: CreateGroupParams): Promise<Group>;
	editGroup(params: EditGroupParams): Promise<Group>;
	downloadServiceRequests(params: FilteredServiceRequestsParams): Promise<Blob>;
	createService(params: CreateServiceParams): Promise<CreateServiceResponse>;
	updateServicesOrder(params: UpdateServicesOrderParams): Promise<GetServiceListResponse>;
	createAttribute(
		params: CreateServiceDefinitionAttributesParams
	): Promise<CreateServiceDefinitionAttributeResponse>;
	editAttribute(
		params: EditServiceDefinitionAttributeParams
	): Promise<EditServiceDefinitionAttributeResponse>;
	deleteAttribute(params: DeleteAttributeParams): Promise<void>;
	updateAttributesOrder(params: UpdateAttributesOrderParams): Promise<ServiceDefinition>;
	editService(params: EditServiceParams): Promise<Service>;
	deleteService(params: HasServiceCode): Promise<void>;
	updateServiceRequest(
		params: UpdateSensitiveServiceRequestRequest
	): Promise<UpdateSensitiveServiceRequestResponse>;
}

const Libre311ServicePropsSchema = z.object({
	baseURL: z.string(),
	jurisdictionConfig: JurisdictionConfigSchema
});

export type Libre311ServiceProps = z.infer<typeof Libre311ServicePropsSchema> & {
	recaptchaService: RecaptchaService;
};

const ROUTES = {
	getJurisdictionConfig: '/config',
	getServiceList: (params: HasJurisdictionId) =>
		`/services?jurisdiction_id=${params.jurisdiction_id}`,
	getServiceDefinition: (params: HasJurisdictionId & HasServiceCode) =>
		`/services/${params.service_code}?jurisdiction_id=${params.jurisdiction_id}`,
	getServiceRequests: (qParams: URLSearchParams) => `/requests?${qParams.toString()}`,
	postGroup: (params: HasJurisdictionId) =>
		`/jurisdiction-admin/groups/?jurisdiction_id=${params.jurisdiction_id}`,
	patchGroup: (params: HasJurisdictionId & HasGroupId) =>
		`/jurisdiction-admin/groups/${params.group_id}?jurisdiction_id=${params.jurisdiction_id}`,
	getGroupList: (params: HasJurisdictionId) =>
		`/jurisdiction-admin/groups/?jurisdiction_id=${params.jurisdiction_id}`,
	postService: (params: HasJurisdictionId) =>
		`/jurisdiction-admin/services?jurisdiction_id=${params.jurisdiction_id}`,
	patchService: (params: HasJurisdictionId & HasServiceCode) =>
		`/jurisdiction-admin/services/${params.service_code}?jurisdiction_id=${params.jurisdiction_id}`,
	deleteService: (params: HasJurisdictionId & HasServiceCode) =>
		`/jurisdiction-admin/services/${params.service_code}?jurisdiction_id=${params.jurisdiction_id}`,
	postAttribute: (params: HasJurisdictionId & HasServiceCode) =>
		`/jurisdiction-admin/services/${params.service_code}/attributes?jurisdiction_id=${params.jurisdiction_id}`,
	patchAttribute: (params: HasJurisdictionId & HasServiceCode & HasAttributeCode) =>
		`/jurisdiction-admin/services/${params.service_code}/attributes/${params.attribute_code}?jurisdiction_id=${params.jurisdiction_id}`,
	deleteAttribute: (params: DeleteAttributeParams & HasJurisdictionId) =>
		`/jurisdiction-admin/services/${params.serviceCode}/attributes/${params.attributeCode}?jurisdiction_id=${params.jurisdiction_id}`,
	postServiceRequest: (params: HasJurisdictionId) =>
		`/requests?jurisdiction_id=${params.jurisdiction_id}`,
	patchServiceRequest: (service_request_id: number, params: HasJurisdictionId) =>
		`/jurisdiction-admin/requests/${service_request_id}?jurisdiction_id=${params.jurisdiction_id}`,
	getServiceRequest: (params: HasJurisdictionId & HasServiceRequestId) =>
		`/requests/${params.service_request_id}?jurisdiction_id=${params.jurisdiction_id}`,
	getServiceRequestsDownload: (params: URLSearchParams) =>
		`/jurisdiction-admin/requests/download?${params.toString()}`,
	updateServicesOrder: (params: UpdateServicesOrderParams & HasJurisdictionId) =>
		`/jurisdiction-admin/groups/${params.group_id}/services-order?jurisdiction_id=${params.jurisdiction_id}`,
	updateAttributesOrder: (params: UpdateAttributesOrderParams & HasJurisdictionId) =>
		`/jurisdiction-admin/services/${params.service_code}/attributes-order?jurisdiction_id=${params.jurisdiction_id}`
};

export async function getJurisdictionConfig(baseURL: string): Promise<JurisdictionConfig> {
	const res = await axios.get<JurisdictionConfig>(baseURL + ROUTES.getJurisdictionConfig);
	return JurisdictionConfigSchema.parse(res.data);
}

class Libre311ServiceError extends Error {
	name: string;
	constructor(name: string, message?: string, options?: ErrorOptions) {
		super(message, options);
		this.name = name;
	}
}

class UnsupportedImageType extends Libre311ServiceError {
	constructor(message?: string, options?: ErrorOptions) {
		super('UnsupportedImageType', message, options);
	}
}

function toURLSearchParams<T extends CreateServiceRequestParams>(params: T) {
	const urlSearchParams = new URLSearchParams();
	for (const [k, v] of Object.entries(params)) {
		if (!v) continue;
		if (Array.isArray(v)) {
			const resultMap = v.reduce((resultMap, attrRes) => {
				const resArr: string[] = resultMap.get(attrRes.code) ?? [];
				resArr.push(attrRes.value);
				resultMap.set(attrRes.code, resArr);

				return resultMap;
			}, new Map<ServiceCode, string[]>());

			resultMap.forEach((value, code) => {
				urlSearchParams.append(`attribute[${code}]`, value.join(','));
			});
		} else {
			urlSearchParams.set(k, v.toString());
		}
	}
	return urlSearchParams;
}

export class Libre311ServiceImpl implements Libre311Service {
	private authTokenInterceptorId: number = -1;
	private axiosInstance: AxiosInstance;
	private jurisdictionId: JurisdictionId;
	private jurisdictionConfig: JurisdictionConfig;
	private recaptchaService: RecaptchaService;
	public static readonly supportedImageTypes = [
		'image/png',
		'image/jpg',
		'image/jpeg',
		'image/webp'
	];

	public constructor(props: Libre311ServiceProps) {
		Libre311ServicePropsSchema.parse(props);
		this.axiosInstance = axios.create({ baseURL: props.baseURL });
		this.jurisdictionConfig = props.jurisdictionConfig;
		this.jurisdictionId = props.jurisdictionConfig.jurisdiction_id;
		this.recaptchaService = props.recaptchaService;
	}

	public static async create(props: Libre311ServiceProps): Promise<Libre311Service> {
		console.log({ props });
		const jurisdictionConfig = await getJurisdictionConfig(props.baseURL);
		return new Libre311ServiceImpl({ ...props, jurisdictionConfig });
	}

	getJurisdictionConfig(): JurisdictionConfig {
		return this.jurisdictionConfig;
	}

	async reverseGeocode(coords: L.PointTuple): Promise<ReverseGeocodeResponse> {
		const startTime = performance.now();
		const fallbackAddress = `${coords[0].toFixed(6)}, ${coords[1].toFixed(6)}`;
		console.log('[reverseGeocode] Starting reverse geocode for coords:', coords);

		try {
			const fetchStart = performance.now();
			// Use Nominatim reverse geocoding API
			// In development, requests go through Vite proxy (/nominatim) to bypass CORS
			// In production, requests go directly to Nominatim (assuming proper server-side handling)
			const isDev = typeof window !== 'undefined' && window.location.hostname === 'localhost';
			const baseUrl = isDev ? '/nominatim' : 'https://nominatim.openstreetmap.org';
			const url = `${baseUrl}/reverse?format=jsonv2&lat=${coords[0]}&lon=${coords[1]}`;

			console.log('[reverseGeocode] Fetching:', url);
			const res = await axios.get<unknown>(url);
			console.log(
				'[reverseGeocode] Response received in:',
				`${(performance.now() - fetchStart).toFixed(1)}ms`
			);

			const parsed = ReverseGeocodeResponseSchema.parse(res.data);
			console.log('[reverseGeocode] Result:', parsed.display_name);
			console.log('[reverseGeocode] Total time:', `${(performance.now() - startTime).toFixed(1)}ms`);

			return parsed;
		} catch (error) {
			console.error('[reverseGeocode] Error during geocoding:', error);
			console.log('[reverseGeocode] Using fallback coordinates due to error');
			// Return fallback coordinates instead of throwing - allows flow to continue
			return { display_name: fallbackAddress };
		}
	}

	async getServiceList(): Promise<GetServiceListResponse> {
		const res = await this.axiosInstance.get<unknown>(
			ROUTES.getServiceList({ jurisdiction_id: this.jurisdictionId })
		);

		return GetServiceListResponseSchema.parse(res.data);
	}

	async getServiceDefinition(params: HasServiceCode): Promise<ServiceDefinition> {
		const res = await this.axiosInstance.get<unknown>(
			ROUTES.getServiceDefinition({ ...params, ...{ jurisdiction_id: this.jurisdictionId } })
		);
		return ServiceDefinitionSchema.parse(res.data);
	}

	async createGroup(params: CreateGroupParams): Promise<Group> {
		try {
			const res = await this.axiosInstance.post<unknown>(
				ROUTES.postGroup(this.jurisdictionConfig),
				params
			);

			return GroupSchema.parse(res.data);
		} catch (error) {
			console.log(error);
			throw error;
		}
	}

	async getGroupList(): Promise<GetGroupListResponse> {
		const res = await this.axiosInstance.get<unknown>(
			ROUTES.getGroupList({ jurisdiction_id: this.jurisdictionId })
		);

		return GetGroupListResponseSchema.parse(res.data);
	}

	async editGroup(params: EditGroupParams): Promise<Group> {
		try {
			const res = await this.axiosInstance.patch<unknown>(
				ROUTES.patchGroup({
					group_id: params.id,
					jurisdiction_id: this.jurisdictionConfig.jurisdiction_id
				}),
				params
			);

			return GroupSchema.parse(res.data);
		} catch (error) {
			console.log(error);
			throw error;
		}
	}

	async createService(params: CreateServiceParams): Promise<CreateServiceResponse> {
		try {
			const res = await this.axiosInstance.post<unknown>(
				ROUTES.postService(this.jurisdictionConfig),
				params
			);

			return CreateServiceResponseSchema.parse(res.data);
		} catch (error) {
			console.log(error);
			throw error;
		}
	}

	async editService(params: EditServiceParams): Promise<Service> {
		try {
			const res = await this.axiosInstance.patch<unknown>(
				ROUTES.patchService({
					service_code: params.service_code,
					jurisdiction_id: this.jurisdictionConfig.jurisdiction_id
				}),
				params
			);

			return ServiceSchema.parse(res.data);
		} catch (error) {
			console.log(error);
			throw error;
		}
	}

	async updateServicesOrder(params: UpdateServicesOrderParams): Promise<GetServiceListResponse> {
		const res = await this.axiosInstance.patch<unknown>(
			ROUTES.updateServicesOrder({
				...params,
				jurisdiction_id: this.jurisdictionConfig.jurisdiction_id
			}),
			params.services
		);
		return GetServiceListResponseSchema.parse(res.data);
	}

	async deleteService(params: HasServiceCode): Promise<void> {
		try {
			await this.axiosInstance.delete<unknown>(
				ROUTES.deleteService({
					...params,
					jurisdiction_id: this.jurisdictionConfig.jurisdiction_id
				})
			);
		} catch (error) {
			console.log(error);
			throw error;
		}
	}

	async createAttribute(
		params: CreateServiceDefinitionAttributesParams
	): Promise<CreateServiceDefinitionAttributeResponse> {
		const res = await this.axiosInstance.post<unknown>(
			ROUTES.postAttribute({
				service_code: params.service_code,
				jurisdiction_id: this.jurisdictionConfig.jurisdiction_id
			}),
			params
		);

		return CreateServiceDefinitionAttributeResponseSchema.parse(res.data);
	}

	async editAttribute(
		params: EditServiceDefinitionAttributeParams
	): Promise<EditServiceDefinitionAttributeResponse> {
		const res = await this.axiosInstance.patch<unknown>(
			ROUTES.patchAttribute({
				service_code: params.service_code,
				attribute_code: params.attribute_code,
				jurisdiction_id: this.jurisdictionConfig.jurisdiction_id
			}),
			params
		);

		return EditServiceDefinitionAttributeResponseSchema.parse(res.data);
	}

	async deleteAttribute(params: DeleteAttributeParams): Promise<void> {
		await this.axiosInstance.delete<void>(
			ROUTES.deleteAttribute({ ...params, jurisdiction_id: this.jurisdictionId })
		);
	}

	async updateAttributesOrder(params: UpdateAttributesOrderParams): Promise<ServiceDefinition> {
		const res = await this.axiosInstance.patch<unknown>(
			ROUTES.updateAttributesOrder({
				...params,
				jurisdiction_id: this.jurisdictionConfig.jurisdiction_id
			}),
			params.attributes
		);
		return ServiceDefinitionSchema.parse(res.data);
	}

	async createServiceRequest(
		params: CreateServiceRequestParams
	): Promise<CreateServiceRequestResponse> {
		const paramsWithRecapta = await this.recaptchaService.wrapWithRecaptcha(
			params,
			'create_service_request'
		);
		const urlSearchparams = toURLSearchParams(paramsWithRecapta);

		const res = await this.axiosInstance.post<InternalCreateServiceRequestResponse>(
			ROUTES.postServiceRequest(this.jurisdictionConfig),
			urlSearchparams
		);

		return InternalCreateServiceRequestResponseSchema.parse(res.data)[0];
	}

	async updateServiceRequest(
		params: UpdateSensitiveServiceRequestRequest
	): Promise<UpdateSensitiveServiceRequestResponse> {
		const res = await this.axiosInstance.patch<unknown>(
			ROUTES.patchServiceRequest(params.service_request_id, this.jurisdictionConfig),
			params
		);

		return CreateServiceRequestResponseSchema.parse(res.data);
	}

	async downloadServiceRequests(params: FilteredServiceRequestsParams): Promise<Blob> {
		const queryParams = FilteredServiceRequestsParamsMapper.toURLSearchParams(params);
		queryParams.append('jurisdiction_id', this.jurisdictionId);
		const res = await this.axiosInstance.get<Blob>(ROUTES.getServiceRequestsDownload(queryParams), {
			responseType: 'blob'
		});
		return res.data;
	}

	async getServiceRequests(
		params: FilteredServiceRequestsParams
	): Promise<ServiceRequestsResponse> {
		const queryParams = FilteredServiceRequestsParamsMapper.toURLSearchParams(params);
		queryParams.append('jurisdiction_id', this.jurisdictionId);

		try {
			const res = await this.axiosInstance.get<unknown>(ROUTES.getServiceRequests(queryParams));
			const serviceRequests = GetServiceRequestsResponseSchema.parse(res.data);
			const pagination: Pagination = {
				offset: Number(res.headers['page-offset']),
				pageNumber: Number(res.headers['page-pagenumber']),
				size: Number(res.headers['page-size']),
				totalPages: Number(res.headers['page-totalpages']),
				totalSize: Number(res.headers['page-totalsize'])
			};

			return {
				serviceRequests,
				metadata: {
					pagination
				}
			};
		} catch (error) {
			console.log(error);
			throw error;
		}
	}

	async getServiceRequest(params: HasServiceRequestId): Promise<ServiceRequest> {
		try {
			const res = await this.axiosInstance.get<unknown>(
				ROUTES.getServiceRequest({ ...params, jurisdiction_id: this.jurisdictionId })
			);
			// the server returns an array with a single object.  Even though it is rather non-standard, it is how it was detailed in the spec.
			return GetServiceRequestsResponseSchema.parse(res.data)[0];
		} catch (error) {
			console.log(error);
			throw error;
		}
	}

	async uploadImage(file: File): Promise<string> {
		if (!Libre311ServiceImpl.supportedImageTypes.includes(file.type)) {
			throw new UnsupportedImageType(
				`Supported image types are ${Libre311ServiceImpl.supportedImageTypes.join(', ')}`
			);
		}
		const formData = new FormData();
		formData.append('file', file);
		const token = await this.recaptchaService.execute('upload_image');
		formData.append('g_recaptcha_response', token);

		const res = await this.axiosInstance.post<unknown>('/image', formData);
		return urlSchema.parse(res.data);
	}

	setAuthInfo(authInfo: UnityAuthLoginResponse | undefined): void {
		if (authInfo) {
			this.authTokenInterceptorId = this.axiosInstance.interceptors.request.use(function (config) {
				config.headers['Authorization'] = `Bearer ${authInfo.access_token}`;
				return config;
			});
		} else {
			this.axiosInstance.interceptors.request.eject(this.authTokenInterceptorId);
		}
	}
}

export function libre311Factory(props: Libre311ServiceProps): Libre311Service {
	return new Libre311ServiceImpl(props);
}
