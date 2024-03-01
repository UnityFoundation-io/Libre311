import type { AxiosInstance } from 'axios';
import axios from 'axios';
import { z } from 'zod';
import type { RecaptchaService } from '../RecaptchaService';
import type {
	UpdateSensitiveServiceRequestRequest,
	UpdateSensitiveServiceRequestResponse
} from './types/UpdateSensitiveServiceRequest';
import type { UnityAuthLoginResponse } from '../UnityAuth/UnityAuth';

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

export type HasServiceCode = z.infer<typeof HasServiceCodeSchema>;
export type ServiceCode = z.infer<typeof ServiceCodeSchema>;

export const ServiceSchema = z
	.object({
		service_name: z.string(),
		description: z.string(),
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

export const OpenServiceRequestStatusSchema = z.literal('open');
export const ClosedServiceRequestStatusSchema = z.literal('closed');
export const ServiceRequestStatusSchema = z.union([
	OpenServiceRequestStatusSchema,
	ClosedServiceRequestStatusSchema
]);
export type ServiceRequestStatus = z.infer<typeof ServiceRequestStatusSchema>;
const urlSchema = z.string().url();

// represents the users responses to the various service definition attributes
const SelectedValuesSchema = z.object({
	code: z.string(),
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
		selected_values: z.array(SelectedValuesSchema).optional()
	})
	.merge(HasServiceRequestIdSchema)
	.merge(HasServiceCodeSchema)
	.merge(ContactInformationSchema);

export type ServiceRequest = z.infer<typeof ServiceRequestSchema>;

export const GetServiceRequestsResponseSchema = z.array(ServiceRequestSchema);
export type GetServiceRequestsResponse = z.infer<typeof GetServiceRequestsResponseSchema>;

export type GetServiceRequestsParams =
	| ServiceRequestId[]
	| {
			serviceCode?: ServiceCode;
			startDate?: string;
			endDate?: string;
			status?: ServiceRequestStatus[];
			pageNumber?: number;
	  };

const latLngTupleSchema = z.tuple([z.number(), z.number()]);

type LatLngTuple = z.infer<typeof latLngTupleSchema>;

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
	updateServiceRequest(
		params: UpdateSensitiveServiceRequestRequest
	): Promise<UpdateSensitiveServiceRequestResponse>;
	getAllServiceRequests(params: GetServiceRequestsParams): Promise<ServiceRequest[]>;
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
	postServiceRequest: (params: HasJurisdictionId) =>
		`/requests?jurisdiction_id=${params.jurisdiction_id}`,
	patchServiceRequest: (service_request_id: number, params: HasJurisdictionId) =>
		`/jurisdiction-admin/requests/${service_request_id}?jurisdiction_id=${params.jurisdiction_id}`,
	getServiceRequest: (params: HasJurisdictionId & HasServiceRequestId) =>
		`/requests/${params.service_request_id}?jurisdiction_id=${params.jurisdiction_id}`
};

export async function getJurisdictionConfig(baseURL: string): Promise<JurisdictionConfig> {
	const res = await axios.get<JurisdictionConfig>(baseURL + ROUTES.getJurisdictionConfig);

	// todo parse the data to validate once the jurisdiction config returns bounds and remove the hardcoded bounds
	const jurisdictionBounds: LatLngTuple[] = [[41.31742721517005, -72.93918211751856]];
	res.data.bounds = jurisdictionBounds;
	return res.data;
	// return JurisdictionConfigSchema.parse(res.data);
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
		queryParams.append('status', params.status?.join(',') ?? '');
		queryParams.append('service_code', params.serviceCode ?? '');
		queryParams.append('start_date', params.startDate ?? '');
		queryParams.append('end_date', params.endDate ?? '');
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
		// todo remove once backend returns bounds info
		const jurisdictionBounds: LatLngTuple[] = [[41.31742721517005, -72.93918211751856]];
		jurisdictionConfig.bounds = jurisdictionBounds;
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
		const res = await this.axiosInstance.patch<InternalCreateServiceRequestResponse>(
			ROUTES.patchServiceRequest(params.service_request_id, this.jurisdictionConfig),
			params
		);

		return InternalCreateServiceRequestResponseSchema.parse(res.data)[0];
	}

	async getAllServiceRequests(params: GetServiceRequestsParams): Promise<ServiceRequest[]> {
		let pageNumber: number = 0;
		const allServiceRequests: ServiceRequest[] = [];
		const queryParams = mapToServiceRequestsURLSearchParams(params);
		queryParams.append('jurisdiction_id', this.jurisdictionId);

		const performRequest = async (
			allServiceRequests: ServiceRequest[]
		): Promise<ServiceRequest[]> => {
			const newQueryParams = mapToServiceRequestsURLSearchParams({ pageNumber: pageNumber });
			newQueryParams.append('jurisdiction_id', this.jurisdictionId);

			try {
				// first request
				const res = await this.axiosInstance.get<unknown>(
					ROUTES.getServiceRequests(newQueryParams)
				);
				const headers = res.headers;
				const totalSize = headers['page-totalsize'];
				const serviceRequests = GetServiceRequestsResponseSchema.parse(res.data);

				if (allServiceRequests.length == 0) allServiceRequests = serviceRequests;
				else allServiceRequests = allServiceRequests.concat(serviceRequests);

				if (allServiceRequests.length < totalSize) {
					// recursive requests
					pageNumber = pageNumber + 1;
					return await performRequest(allServiceRequests);
				}

				return allServiceRequests;
			} catch (error) {
				console.log(error);
				throw error;
			}
		};

		return await performRequest(allServiceRequests);
	}

	async getServiceRequests(params: GetServiceRequestsParams): Promise<ServiceRequestsResponse> {
		const queryParams = mapToServiceRequestsURLSearchParams(params);
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
		const token = await this.recaptchaService.execute('upload_image');
		const asDataUrl = await this.convertToDataURL(file);
		const res = await this.axiosInstance.post<unknown>('/image', {
			image: asDataUrl,
			g_recaptcha_response: token
		});

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

	private async convertToDataURL(file: File): Promise<string> {
		return new Promise((resolve, reject) => {
			const reader = new FileReader();
			reader.addEventListener(
				'load',
				() => {
					if (typeof reader.result === 'string') {
						resolve(reader.result);
					} else {
						reject('Unsupported Operation');
					}
				},
				false
			);
			reader.readAsDataURL(file);
		});
	}
}

export function libre311Factory(props: Libre311ServiceProps): Libre311Service {
	return new Libre311ServiceImpl(props);
}
