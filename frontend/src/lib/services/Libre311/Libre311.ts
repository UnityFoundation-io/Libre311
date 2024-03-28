import type { AxiosInstance } from 'axios';
import axios from 'axios';
import { z } from 'zod';
import type { RecaptchaService } from '../RecaptchaService';
import type {
	UpdateSensitiveServiceRequestRequest,
	UpdateSensitiveServiceRequestResponse
} from './types/UpdateSensitiveServiceRequest';
import type { UnityAuthLoginResponse } from '../UnityAuth/UnityAuth';
import type { HasId } from '$lib/utils/types';

const JurisdicationIdSchema = z.string();
const HasJurisdictionIdSchema = z.object({
	jurisdiction_id: JurisdicationIdSchema
});
export type HasJurisdictionId = z.infer<typeof HasJurisdictionIdSchema>;
export type JurisdictionId = z.infer<typeof JurisdicationIdSchema>;

const RealtimeServiceTypeSchema = z.literal('realtime');
const OtherServiceTypeSchema = z.literal('other'); // todo remove once second type is found
const ServiceTypeSchema = z.union([RealtimeServiceTypeSchema, OtherServiceTypeSchema]); // todo what are the other types besides realtime?

const ServiceCodeSchema = z.string();
const HasServiceCodeSchema = z.object({
	service_code: ServiceCodeSchema
});

export const GroupSchema = z.object({ id: z.number(), name: z.string() });
export type Group = z.infer<typeof GroupSchema>;

export type HasServiceCode = z.infer<typeof HasServiceCodeSchema>;
export type ServiceCode = z.infer<typeof ServiceCodeSchema>;

export const ServiceSchema = z
	.object({
		id: z.number(),
		service_name: z.string(),
		description: z.string().optional(),
		metadata: z.boolean(),
		type: ServiceTypeSchema
		// keywords: z.array(z.string()),
		// group: z.string()
	})
	.merge(HasServiceCodeSchema);

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
	code: z.string(),
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
	key: z.string().optional(),
	/**
	 * The human readable title of an option for singlevaluelist or multivaluelist. This is analogous to the innerhtml text node of an html option tag.
	 */
	name: z.string().optional()
});

export type AttributeValue = z.infer<typeof AttributeValueSchema>;

export const ListBasedServiceDefinitionAttributeSchema =
	BaseServiceDefinitionAttributeSchema.extend({
		datatype: z.union([SingleValueListType, MultiValueListType]),
		values: z.array(AttributeValueSchema).optional()
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

const HasGroupIdSchema = z.object({
	group_id: z.number()
});

export type HasGroupId = z.infer<typeof HasGroupIdSchema>;

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

export function isServiceRequestPriority(
	maybePriority: unknown
): maybePriority is ServiceRequestPriority {
	return maybePriority == 'low' || maybePriority == 'medium' || maybePriority == 'high';
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
export function isServiceRequestStatus(maybeStatus: unknown): maybeStatus is ServiceRequestStatus {
	return maybeStatus == 'open' || maybeStatus == 'closed';
}
const urlSchema = z.string().url();

// represents the users responses to the various service definition attributes
const SelectedValuesSchema = z.object({
	code: z.string(),
	datatype: DatatypeUnionSchema,
	description: z.string(),
	values: z.array(AttributeValueSchema).optional() // key is the SelectOption value and name is the human readable option.  For displaying the value to users, show the name.
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
		id: z.number(),
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
	id: z.number(),
	service_name: z.string()
});

// Edit Service - Request Type
export type EditServiceParams = z.infer<typeof EditServiceParamsSchema>;

// ***************** Delete Service *************** //

export type DeleteServiceParams = {
	serviceId: number;
};

// ***************** Attributes *************** //

export const CreateServiceDefinitionAttributeResponseSchema = z.object({
	service_code: z.string(),
	attributes: z.array(ServiceDefinitionAttributeSchema)
});

export type CreateServiceDefinitionAttributeResponse = z.infer<
	typeof CreateServiceDefinitionAttributeResponseSchema
>;

export const CreateServiceDefinitionAttributesSchema = z.object({
	serviceId: z.number(),
	description: z.string(),
	code: z.string(),
	datatype: z.string(),
	variable: z.boolean(),
	required: z.boolean(),
	order: z.number(),
	values: z.array(ServiceDefinitionAttributeSchema).optional()
});

export type CreateServiceDefinitionAttributesParams = z.infer<
	typeof CreateServiceDefinitionAttributesSchema
>;

// ************************************************ //

export type GetServiceRequestsParams =
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
	getServiceRequests(params: GetServiceRequestsParams): Promise<ServiceRequestsResponse>;
	getServiceRequest(params: HasServiceRequestId): Promise<ServiceRequest>;
}

export const ReverseGeocodeResponseSchema = z.object({
	display_name: z.string()
});
export type ReverseGeocodeResponse = z.infer<typeof ReverseGeocodeResponseSchema>;

export interface Libre311Service extends Open311Service {
	getJurisdictionConfig(): JurisdictionConfig;
	reverseGeocode(coords: L.PointTuple): Promise<ReverseGeocodeResponse>;
	uploadImage(file: File): Promise<string>;
	setAuthInfo(authInfo: UnityAuthLoginResponse | undefined): void;
	getGroupList(): Promise<GetGroupListResponse>;
	createGroup(params: CreateGroupParams): Promise<Group>;
	editGroup(params: EditGroupParams): Promise<Group>;
	downloadServiceRequests(params: URLSearchParams): Promise<Blob>;
	createService(params: CreateServiceParams): Promise<CreateServiceResponse>;
	createAttribute(
		params: CreateServiceDefinitionAttributesParams
	): Promise<CreateServiceDefinitionAttributeResponse>;
	editService(params: EditServiceParams): Promise<Service>;
	deleteService(params: DeleteServiceParams): Promise<void>;
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
	patchService: (params: HasJurisdictionId & HasId<number>) =>
		`/jurisdiction-admin/services/${params.id}?jurisdiction_id=${params.jurisdiction_id}`,
	deleteService: (params: DeleteServiceParams & HasJurisdictionId) =>
		`/jurisdiction-admin/services/${params.serviceId}?jurisdiction_id=${params.jurisdiction_id}`,
	postAttribute: (params: HasId<number> & HasJurisdictionId) =>
		`/jurisdiction-admin/services/${params.id}/attributes?jurisdiction_id=${params.jurisdiction_id}`,
	postServiceRequest: (params: HasJurisdictionId) =>
		`/requests?jurisdiction_id=${params.jurisdiction_id}`,
	patchServiceRequest: (service_request_id: number, params: HasJurisdictionId) =>
		`/jurisdiction-admin/requests/${service_request_id}?jurisdiction_id=${params.jurisdiction_id}`,
	getServiceRequest: (params: HasJurisdictionId & HasServiceRequestId) =>
		`/requests/${params.service_request_id}?jurisdiction_id=${params.jurisdiction_id}`,
	getServiceRequestsDownload: (params: URLSearchParams) =>
		`/jurisdiction-admin/requests/download?${params.toString()}`
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
			urlSearchParams.set(k, v);
		}
	}
	return urlSearchParams;
}

export function mapToServiceRequestsURLSearchParams(params: GetServiceRequestsParams) {
	const queryParams = new URLSearchParams();

	if (Array.isArray(params)) {
		queryParams.append('service_request_id', params.join(','));
	} else {
		queryParams.append('page_size', '10');
		queryParams.append('page', `${params.pageNumber ?? 0}`);
		params.servicePriority && queryParams.append('priority', params.servicePriority?.join(','));
		params.status && queryParams.append('status', params.status?.join(','));
		params.serviceCode && queryParams.append('service_code', params.serviceCode?.join(','));
		params.startDate && queryParams.append('start_date', params.startDate);
		params.endDate && queryParams.append('end_date', params.endDate);
	}
	return queryParams;
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
		const url = `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${coords[0]}&lon=${coords[1]}`;
		const res = await axios.get<unknown>(url);
		return ReverseGeocodeResponseSchema.parse(res.data);
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
					id: params.id,
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

	async deleteService(params: DeleteServiceParams): Promise<void> {
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
				id: params.serviceId,
				jurisdiction_id: this.jurisdictionConfig.jurisdiction_id
			}),
			params
		);

		return CreateServiceDefinitionAttributeResponseSchema.parse(res.data);
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

	async downloadServiceRequests(params: URLSearchParams): Promise<Blob> {
		params.append('jurisdiction_id', this.jurisdictionId);

		try {
			const res = await this.axiosInstance.get<Blob>(ROUTES.getServiceRequestsDownload(params), {
				responseType: 'blob'
			});
			return res.data;
		} catch (error) {
			console.log(error);
			throw error;
		}
	}

	async getServiceRequests(params: GetServiceRequestsParams): Promise<ServiceRequestsResponse> {
		const queryParams = mapToServiceRequestsURLSearchParams(params);
		queryParams.append('jurisdiction_id', this.jurisdictionId);
		console.log(queryParams.toString());

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
