import type { AxiosInstance } from 'axios';
import axios from 'axios';
import { z } from 'zod';

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

type HasServiceCode = z.infer<typeof HasServiceCodeSchema>;
type ServiceCode = z.infer<typeof ServiceCodeSchema>;

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

const DatatypeUnionSchema = z.union([
	StringType,
	NumberType,
	DatetimeType,
	TextType,
	SingleValueListType,
	MultiValueListType
]);

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
	 * A description of the datatype which helps the user provide their input
	 */
	datatype_description: z.string().nullish(), // probably is the helperText
	order: z.number(),
	/**
	 * The actual question
	 */
	description: z.string()
});

export const NonListBasedServiceDefinitionAttributeSchema =
	BaseServiceDefinitionAttributeSchema.extend({
		datatype: z.union([StringType, NumberType, DatetimeType, TextType])
	});

const AttributeValueSchema = z.object({
	/**
	 * The unique identifier associated with an option for singlevaluelist or multivaluelist. This is analogous to the value attribute in an html option tag.
	 */
	key: z.number(),
	/**
	 * The human readable title of an option for singlevaluelist or multivaluelist. This is analogous to the innerhtml text node of an html option tag.
	 */
	name: z.string()
});
export const ListBasedServiceDefinitionAttributeSchema =
	BaseServiceDefinitionAttributeSchema.extend({
		datatype: z.union([SingleValueListType, MultiValueListType]),
		values: z.array(AttributeValueSchema)
	});

export const ServiceDefinitionAttributeSchema = z.union([
	NonListBasedServiceDefinitionAttributeSchema,
	ListBasedServiceDefinitionAttributeSchema
]);

type ServiceDefinitionAttribute = z.infer<typeof ServiceDefinitionAttributeSchema>;

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

export const GetServiceListResponseSchema = z.array(ServiceSchema);
export type GetServiceListResponse = z.infer<typeof GetServiceListResponseSchema>;

// user response values from  ServiceDefinitionAttributeSchema.
// attribute[code1]=value1
// ServiceDefinitionAttributeCode
type AttributeResponse = { code: ServiceDefinitionAttribute['code']; value: string };
// todo revisit params, they list a ton of optional that we may want to require
// todo will likely need the recaptcha value here
export type CreateServiceRequestParams = HasServiceCode & {
	lat: string;
	lng: string;
	address_string: string;
	attributes: AttributeResponse[];
	description: string;
	media_url?: string;
};

export const OpenServiceRequestStatusSchema = z.literal('Open');
export const ClosedServiceRequestStatusSchema = z.literal('Closed');
export const ServiceRequestStatusSchema = z.union([
	OpenServiceRequestStatusSchema,
	ClosedServiceRequestStatusSchema
]);
export type ServiceRequestStatus = z.infer<typeof ServiceRequestStatusSchema>;

export const ServiceRequestSchema = z
	.object({
		status: ServiceRequestStatusSchema,
		status_notes: z.string().nullish(),
		service_name: z.string(),
		description: z.string().nullish(), // this seems like it should be required as a bareminimum
		detail: z.array(z.string()),
		agency_responsible: z.string().nullish(), // SeeClickFix guarantees this as known at time of creation
		service_notice: z.string().nullish(),
		requested_datetime: z.string(),
		updated_datetime: z.string(),
		expected_datetime: z.string().nullish(),
		address: z.string(),
		address_id: z.number().nullish(),
		zipcode: z.string(),
		lat: z.string(),
		long: z.string(),
		media_url: z.string().nullish()
	})
	.merge(HasServiceRequestIdSchema)
	.merge(HasServiceCodeSchema);

export type ServiceRequest = z.infer<typeof ServiceRequestSchema>;

export const GetServiceRequestsResponseSchema = z.array(ServiceRequestSchema);
export type GetServiceRequestsResponse = z.infer<typeof GetServiceRequestsResponseSchema>;

// todo add pagination params
type GetServiceRequestsParams =
	| ServiceRequestId[]
	| {
			service_code?: ServiceCode;
			start_date?: string;
			end_date?: string;
			status?: ServiceRequestStatus[];
	  };

// https://wiki.open311.org/GeoReport_v2/
export interface Open311Service {
	getServiceList(): Promise<GetServiceListResponse>;
	getServiceDefinition(params: HasServiceCode): Promise<ServiceDefinition>;
	createServiceRequest(params: CreateServiceRequestParams): Promise<CreateServiceRequestResponse>;
	getServiceRequests(params: GetServiceRequestsParams): Promise<GetServiceRequestsResponse>;
	getServiceRequest(params: HasServiceRequestId): Promise<ServiceRequest>;
}

const JurisdictionConfigSchema = z
	.object({
		jurisdiction_name: z.string()
	})
	.merge(HasJurisdictionIdSchema);

type JurisdictionConfig = z.infer<typeof JurisdictionConfigSchema>;

export interface Libre311Service extends Open311Service {
	getJurisdictionConfig(): JurisdictionConfig;
}

export type Libre311ServiceProps = {
	baseURL: string;
};

const ROUTES = {
	getJurisdictionConfig: '/config',
	getServiceList: (params: HasJurisdictionId) =>
		`/services?jurisdiction_id=${params.jurisdiction_id}`,
	getServiceDefinition: (params: HasJurisdictionId & HasServiceCode) =>
		`/services/${params.service_code}?jurisdiction_id=${params.jurisdiction_id}`
};

// eslint-disable-next-line @typescript-eslint/no-unused-vars
async function getJurisdictionConfig(): Promise<JurisdictionConfig> {
	const res = await axios.get<unknown>(ROUTES.getJurisdictionConfig);
	return JurisdictionConfigSchema.parse(res.data);
}

export class Libre311ServiceImpl implements Libre311Service {
	private axiosInstance: AxiosInstance;
	private jurisdictionId: JurisdictionId;
	private jurisdictionConfig: JurisdictionConfig;

	private constructor(props: Libre311ServiceProps & { jurisdictionConfig: JurisdictionConfig }) {
		this.axiosInstance = axios.create({ baseURL: props.baseURL });
		this.jurisdictionConfig = props.jurisdictionConfig;
		this.jurisdictionId = props.jurisdictionConfig.jurisdiction_id;
	}

	public static async create(props: Libre311ServiceProps) {
		// todo uncomment when /config endpoint exists code the jurisdiction_id
		// const jurisdictionConfig = await getJurisdictionConfig();
		const jurisdictionConfig = {
			jurisdiction_id: 'town.gov',
			jurisdiction_name: 'Fayetteville, AR'
		};
		return new Libre311ServiceImpl({ ...props, jurisdictionConfig });
	}

	getJurisdictionConfig(): JurisdictionConfig {
		return this.jurisdictionConfig;
	}

	async getServiceList(): Promise<GetServiceListResponse> {
		const res = await this.axiosInstance.get<unknown>(
			ROUTES.getServiceList({ jurisdiction_id: this.jurisdictionId })
		);
		return GetServiceListResponseSchema.parse(res.data);
	}

	async getServiceDefinition(params: HasServiceCode): Promise<ServiceDefinition> {
		const res = await axios.get<unknown>(
			ROUTES.getServiceDefinition({ ...params, ...{ jurisdiction_id: this.jurisdictionId } })
		);
		return ServiceDefinitionSchema.parse(res.data);
	}

	async createServiceRequest(
		params: CreateServiceRequestParams
	): Promise<CreateServiceRequestResponse> {
		console.log(params);
		throw Error('Not Implemented');
	}

	async getServiceRequests(params: GetServiceRequestsParams): Promise<GetServiceRequestsResponse> {
		console.log(params);
		throw Error('Not Implemented');
	}

	async getServiceRequest(params: HasServiceRequestId): Promise<ServiceRequest> {
		console.log(params);
		throw Error('Not Implemented');
	}
}
