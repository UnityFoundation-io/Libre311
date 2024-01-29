import type { PointTuple } from 'leaflet';
import {
	type GetServiceRequestsParams,
	type Libre311Service,
	type Pagination,
	type ServiceRequestsResponse,
	type CreateServiceRequestParams,
	type CreateServiceRequestResponse,
	type HasServiceRequestId,
	type ServiceRequest,
	type GetServiceListResponse,
	type JurisdictionConfig,
	type ServiceDefinition,
	type HasServiceCode,
	type Libre311ServiceProps,
	ReverseGeocodeResponseSchema
} from './Libre311';
import axios from 'axios';

const serviceRequests: ServiceRequest[] = [
	{
		description:
			'City of New Haven stop posting automatic responses because people may think that when they post it gets immediate human  attention',
		address: '200 Orange St New Haven, Connecticut, 06510',
		lat: '41.3071110182325',
		service_request_id: 1,
		requested_datetime: '2023-03-29T14:36:43-04:00',
		long: '-72.9235610239783',
		service_code: '51',
		status: 'Closed',
		zipcode: '06510',
		updated_datetime: '2023-03-31T08:04:22-04:00',
		agency_responsible: 'City of New Haven',
		service_name: 'Traffic Signal / Pedestrian Signal',
		media_url: null, //'http://tinyurl.com/ufskhh54',
		expected_datetime: null,
		address_id: null,
		service_notice: null,
		status_notes: null
	},
	{
		description:
			'Bus shelter on 270 Temple St. New Haven broken glass that could cause injury to a person. ',
		address: '270 Temple St New Haven, CT, 06511, USA',
		lat: '41.3083092093462',
		service_request_id: 2,
		requested_datetime: '2023-03-27T15:05:50-04:00',
		long: '-72.9258607025516',
		service_code: '22112',
		status: 'Open',
		zipcode: '06511',
		updated_datetime: '2023-03-30T11:38:16-04:00',
		agency_responsible: 'City of New Haven',
		service_name: 'Bus Shelter Repair',
		media_url:
			'https://seeclickfix.com/rails/active_storage/representations/redirect/eyJfcmFpbHMiOnsibWVzc2FnZSI6IkJBaHBBMDJycmc9PSIsImV4cCI6bnVsbCwicHVyIjoiYmxvYl9pZCJ9fQ==--0e11181caadcd8da758f96b7196a69f8d20f5388/eyJfcmFpbHMiOnsibWVzc2FnZSI6IkJBaDdDVG9MWm05eWJXRjBTU0lJYW5CbkJqb0dSVlE2RUdGMWRHOWZiM0pwWlc1MFZEb0tjM1J5YVhCVU9ndHlaWE5wZW1WSklndzRNREI0TmpBd0Jqc0dWQT09IiwiZXhwIjpudWxsLCJwdXIiOiJ2YXJpYXRpb24ifX0=--ee2a5b821fe0552ba6053903421e37a595d06845/Temple%20St.%20&%20Elm%20St%20Bus%20Stop%20Shelter%20Window%20Damage%20(4).jpg',
		expected_datetime: '2023-03-30T11:38:16-04:00',
		address_id: null,
		service_notice: null,
		status_notes: null
	},
	{
		description: 'Street lamp is out',
		address: '950 Chapel St New Haven, CT, 06510, USA',
		lat: '41.3067298619134',
		service_request_id: 3,
		requested_datetime: '2023-03-24T10:34:58-04:00',
		long: '-72.9277214850559',
		service_code: '124',
		status: 'Open',
		zipcode: '06510',
		updated_datetime: '2023-03-29T13:30:24-04:00',
		agency_responsible: 'City of New Haven',
		service_name: 'Street Lamp',
		media_url:
			'https://seeclickfix.com/rails/active_storage/representations/redirect/eyJfcmFpbHMiOnsibWVzc2FnZSI6IkJBaHBBMG5EclE9PSIsImV4cCI6bnVsbCwicHVyIjoiYmxvYl9pZCJ9fQ==--7141205ed34545a04440e417df679bca3483df81/eyJfcmFpbHMiOnsibWVzc2FnZSI6IkJBaDdDVG9MWm05eWJXRjBTU0lKYW5CbFp3WTZCa1ZVT2hCaGRYUnZYMjl5YVdWdWRGUTZDbk4wY21sd1ZEb0xjbVZ6YVhwbFNTSU1PREF3ZURZd01BWTdCbFE9IiwiZXhwIjpudWxsLCJwdXIiOiJ2YXJpYXRpb24ifX0=--09f1d8a44f20164c6fe72999dd865d9ba60da451/3F112374-6E7A-4BC3-91C5-221A2251ADBA.jpeg',
		expected_datetime: null,
		address_id: null,
		service_notice: null,
		status_notes: null
	},
	{
		description:
			'City of New Haven stop posting automatic responses because people may think that when they post it gets immediate human  attention',
		address: '200 Orange St New Haven, Connecticut, 06510',
		lat: '41.307941',
		service_request_id: 4,
		requested_datetime: '2023-03-29T14:36:43-04:00',
		long: '-72.916881',
		service_code: '51',
		status: 'Closed',
		zipcode: '06510',
		updated_datetime: '2023-03-31T08:04:22-04:00',
		agency_responsible: 'City of New Haven',
		service_name: 'Traffic Signal / Pedestrian Signal',
		media_url: null, //'http://tinyurl.com/ufskhh54',
		expected_datetime: null,
		address_id: null,
		service_notice: null,
		status_notes: null
	},
	{
		description:
			'Bus shelter on 270 Temple St. New Haven broken glass that could cause injury to a person. ',
		address: '270 Temple St New Haven, CT, 06511, USA',
		lat: '41.318331',
		service_request_id: 5,
		requested_datetime: '2023-03-27T15:05:50-04:00',
		long: '-72.928335',
		service_code: '22112',
		status: 'Open',
		zipcode: '06511',
		updated_datetime: '2023-03-30T11:38:16-04:00',
		agency_responsible: 'City of New Haven',
		service_name: 'Bus Shelter Repair',
		media_url:
			'https://seeclickfix.com/rails/active_storage/representations/redirect/eyJfcmFpbHMiOnsibWVzc2FnZSI6IkJBaHBBMDJycmc9PSIsImV4cCI6bnVsbCwicHVyIjoiYmxvYl9pZCJ9fQ==--0e11181caadcd8da758f96b7196a69f8d20f5388/eyJfcmFpbHMiOnsibWVzc2FnZSI6IkJBaDdDVG9MWm05eWJXRjBTU0lJYW5CbkJqb0dSVlE2RUdGMWRHOWZiM0pwWlc1MFZEb0tjM1J5YVhCVU9ndHlaWE5wZW1WSklndzRNREI0TmpBd0Jqc0dWQT09IiwiZXhwIjpudWxsLCJwdXIiOiJ2YXJpYXRpb24ifX0=--ee2a5b821fe0552ba6053903421e37a595d06845/Temple%20St.%20&%20Elm%20St%20Bus%20Stop%20Shelter%20Window%20Damage%20(4).jpg',
		expected_datetime: '2023-03-30T11:38:16-04:00',
		address_id: null,
		service_notice: null,
		status_notes: null
	},
	{
		description: 'Street lamp is out',
		address: '950 Chapel St New Haven, CT, 06510, USA',
		lat: '41.316726',
		service_request_id: 6,
		requested_datetime: '2023-03-24T10:34:58-04:00',
		long: '-72.944871',
		service_code: '124',
		status: 'Open',
		zipcode: '06510',
		updated_datetime: '2023-03-29T13:30:24-04:00',
		agency_responsible: 'City of New Haven',
		service_name: 'Street Lamp',
		media_url:
			'https://seeclickfix.com/rails/active_storage/representations/redirect/eyJfcmFpbHMiOnsibWVzc2FnZSI6IkJBaHBBMG5EclE9PSIsImV4cCI6bnVsbCwicHVyIjoiYmxvYl9pZCJ9fQ==--7141205ed34545a04440e417df679bca3483df81/eyJfcmFpbHMiOnsibWVzc2FnZSI6IkJBaDdDVG9MWm05eWJXRjBTU0lKYW5CbFp3WTZCa1ZVT2hCaGRYUnZYMjl5YVdWdWRGUTZDbk4wY21sd1ZEb0xjbVZ6YVhwbFNTSU1PREF3ZURZd01BWTdCbFE9IiwiZXhwIjpudWxsLCJwdXIiOiJ2YXJpYXRpb24ifX0=--09f1d8a44f20164c6fe72999dd865d9ba60da451/3F112374-6E7A-4BC3-91C5-221A2251ADBA.jpeg',
		expected_datetime: null,
		address_id: null,
		service_notice: null,
		status_notes: null
	},
	{
		description:
			'City of New Haven stop posting automatic responses because people may think that when they post it gets immediate human  attention',
		address: '200 Orange St New Haven, Connecticut, 06510',
		lat: '41.310274',
		service_request_id: 7,
		requested_datetime: '2023-03-29T14:36:43-04:00',
		long: '-72.947411',
		service_code: '51',
		status: 'Closed',
		zipcode: '06510',
		updated_datetime: '2023-03-31T08:04:22-04:00',
		agency_responsible: 'City of New Haven',
		service_name: 'Traffic Signal / Pedestrian Signal',
		media_url: null, //'http://tinyurl.com/ufskhh54',
		expected_datetime: null,
		address_id: null,
		service_notice: null,
		status_notes: null
	},
	{
		description:
			'Bus shelter on 270 Temple St. New Haven broken glass that could cause injury to a person. ',
		address: '270 Temple St New Haven, CT, 06511, USA',
		lat: '41.295307',
		service_request_id: 8,
		requested_datetime: '2023-03-27T15:05:50-04:00',
		long: '-72.935272',
		service_code: '22112',
		status: 'Open',
		zipcode: '06511',
		updated_datetime: '2023-03-30T11:38:16-04:00',
		agency_responsible: 'City of New Haven',
		service_name: 'Bus Shelter Repair',
		media_url:
			'https://seeclickfix.com/rails/active_storage/representations/redirect/eyJfcmFpbHMiOnsibWVzc2FnZSI6IkJBaHBBMDJycmc9PSIsImV4cCI6bnVsbCwicHVyIjoiYmxvYl9pZCJ9fQ==--0e11181caadcd8da758f96b7196a69f8d20f5388/eyJfcmFpbHMiOnsibWVzc2FnZSI6IkJBaDdDVG9MWm05eWJXRjBTU0lJYW5CbkJqb0dSVlE2RUdGMWRHOWZiM0pwWlc1MFZEb0tjM1J5YVhCVU9ndHlaWE5wZW1WSklndzRNREI0TmpBd0Jqc0dWQT09IiwiZXhwIjpudWxsLCJwdXIiOiJ2YXJpYXRpb24ifX0=--ee2a5b821fe0552ba6053903421e37a595d06845/Temple%20St.%20&%20Elm%20St%20Bus%20Stop%20Shelter%20Window%20Damage%20(4).jpg',
		expected_datetime: '2023-03-30T11:38:16-04:00',
		address_id: null,
		service_notice: null,
		status_notes: null
	},
	{
		description: 'Street lamp is out',
		address: '950 Chapel St New Haven, CT, 06510, USA',
		lat: '41.307699',
		service_request_id: 9,
		requested_datetime: '2023-03-24T10:34:58-04:00',
		long: '-72.932086',
		service_code: '124',
		status: 'Open',
		zipcode: '06510',
		updated_datetime: '2023-03-29T13:30:24-04:00',
		agency_responsible: 'City of New Haven',
		service_name: 'Street Lamp',
		media_url:
			'https://seeclickfix.com/rails/active_storage/representations/redirect/eyJfcmFpbHMiOnsibWVzc2FnZSI6IkJBaHBBMG5EclE9PSIsImV4cCI6bnVsbCwicHVyIjoiYmxvYl9pZCJ9fQ==--7141205ed34545a04440e417df679bca3483df81/eyJfcmFpbHMiOnsibWVzc2FnZSI6IkJBaDdDVG9MWm05eWJXRjBTU0lKYW5CbFp3WTZCa1ZVT2hCaGRYUnZYMjl5YVdWdWRGUTZDbk4wY21sd1ZEb0xjbVZ6YVhwbFNTSU1PREF3ZURZd01BWTdCbFE9IiwiZXhwIjpudWxsLCJwdXIiOiJ2YXJpYXRpb24ifX0=--09f1d8a44f20164c6fe72999dd865d9ba60da451/3F112374-6E7A-4BC3-91C5-221A2251ADBA.jpeg',
		expected_datetime: null,
		address_id: null,
		service_notice: null,
		status_notes: null
	},
	{
		description:
			'City of New Haven stop posting automatic responses because people may think that when they post it gets immediate human  attention',
		address: '200 Orange St New Haven, Connecticut, 06510',
		lat: '41.312727',
		service_request_id: 10,
		requested_datetime: '2023-03-29T14:36:43-04:00',
		long: '-72.928617',
		service_code: '51',
		status: 'Closed',
		zipcode: '06510',
		updated_datetime: '2023-03-31T08:04:22-04:00',
		agency_responsible: 'City of New Haven',
		service_name: 'Traffic Signal / Pedestrian Signal',
		media_url: null, //'http://tinyurl.com/ufskhh54',
		expected_datetime: null,
		address_id: null,
		service_notice: null,
		status_notes: null
	},
	{
		description:
			'Bus shelter on 270 Temple St. New Haven broken glass that could cause injury to a person. ',
		address: '270 Temple St New Haven, CT, 06511, USA',
		lat: '41.317514',
		service_request_id: 11,
		requested_datetime: '2023-03-27T15:05:50-04:00',
		long: '-72.929585',
		service_code: '22112',
		status: 'Open',
		zipcode: '06511',
		updated_datetime: '2023-03-30T11:38:16-04:00',
		agency_responsible: 'City of New Haven',
		service_name: 'Bus Shelter Repair',
		media_url:
			'https://seeclickfix.com/rails/active_storage/representations/redirect/eyJfcmFpbHMiOnsibWVzc2FnZSI6IkJBaHBBMDJycmc9PSIsImV4cCI6bnVsbCwicHVyIjoiYmxvYl9pZCJ9fQ==--0e11181caadcd8da758f96b7196a69f8d20f5388/eyJfcmFpbHMiOnsibWVzc2FnZSI6IkJBaDdDVG9MWm05eWJXRjBTU0lJYW5CbkJqb0dSVlE2RUdGMWRHOWZiM0pwWlc1MFZEb0tjM1J5YVhCVU9ndHlaWE5wZW1WSklndzRNREI0TmpBd0Jqc0dWQT09IiwiZXhwIjpudWxsLCJwdXIiOiJ2YXJpYXRpb24ifX0=--ee2a5b821fe0552ba6053903421e37a595d06845/Temple%20St.%20&%20Elm%20St%20Bus%20Stop%20Shelter%20Window%20Damage%20(4).jpg',
		expected_datetime: '2023-03-30T11:38:16-04:00',
		address_id: null,
		service_notice: null,
		status_notes: null
	},
	{
		description: 'Street lamp is out',
		address: '950 Chapel St New Haven, CT, 06510, USA',
		lat: '41.336396',
		service_request_id: 12,
		requested_datetime: '2023-03-24T10:34:58-04:00',
		long: '-72.888005',
		service_code: '124',
		status: 'Open',
		zipcode: '06510',
		updated_datetime: '2023-03-29T13:30:24-04:00',
		agency_responsible: 'City of New Haven',
		service_name: 'Street Lamp',
		media_url:
			'https://seeclickfix.com/rails/active_storage/representations/redirect/eyJfcmFpbHMiOnsibWVzc2FnZSI6IkJBaHBBMG5EclE9PSIsImV4cCI6bnVsbCwicHVyIjoiYmxvYl9pZCJ9fQ==--7141205ed34545a04440e417df679bca3483df81/eyJfcmFpbHMiOnsibWVzc2FnZSI6IkJBaDdDVG9MWm05eWJXRjBTU0lKYW5CbFp3WTZCa1ZVT2hCaGRYUnZYMjl5YVdWdWRGUTZDbk4wY21sd1ZEb0xjbVZ6YVhwbFNTSU1PREF3ZURZd01BWTdCbFE9IiwiZXhwIjpudWxsLCJwdXIiOiJ2YXJpYXRpb24ifX0=--09f1d8a44f20164c6fe72999dd865d9ba60da451/3F112374-6E7A-4BC3-91C5-221A2251ADBA.jpeg',
		expected_datetime: null,
		address_id: null,
		service_notice: null,
		status_notes: null
	}
];

export class MockLibre311ServiceImpl implements Libre311Service {
	private jurisdictionConfig: JurisdictionConfig;

	private constructor(jurisdictionConfig: JurisdictionConfig) {
		this.jurisdictionConfig = jurisdictionConfig;
	}
	async reverseGeocode(coords: PointTuple): Promise<{ display_name: string }> {
		const url = `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${coords[0]}&lon=${coords[1]}`;
		const res = await axios.get<unknown>(url);
		return ReverseGeocodeResponseSchema.parse(res.data);
	}

	// eslint-disable-next-line @typescript-eslint/no-unused-vars
	public static async create(props: Libre311ServiceProps) {
		// todo uncomment when /config endpoint exists code the jurisdiction_id
		// const jurisdictionConfig = await getJurisdictionConfig();
		const jurisdictionConfig = {
			jurisdiction_id: 'town.gov',
			jurisdiction_name: 'Fayetteville, AR'
		};
		return new MockLibre311ServiceImpl(jurisdictionConfig);
	}

	getJurisdictionConfig(): JurisdictionConfig {
		return this.jurisdictionConfig;
	}

	async getServiceList(): Promise<GetServiceListResponse> {
		throw new Error('Not implemented');
	}

	// eslint-disable-next-line @typescript-eslint/no-unused-vars
	async getServiceDefinition(params: HasServiceCode): Promise<ServiceDefinition> {
		throw new Error('Not implemented');
	}

	async createServiceRequest(
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		params: CreateServiceRequestParams
	): Promise<CreateServiceRequestResponse> {
		throw Error('Not Implemented');
	}

	// eslint-disable-next-line @typescript-eslint/no-unused-vars
	async getServiceRequests(params: GetServiceRequestsParams): Promise<ServiceRequestsResponse> {
		const pagination: Pagination = {
			offset: 0,
			pageNumber: params.pageNumber ?? 0,
			size: 10,
			totalPages: Math.ceil(serviceRequests.length / 10),
			totalSize: serviceRequests.length
		};

		const start = pagination.pageNumber * pagination.size;
		const end = start + pagination.size;

		const requests = serviceRequests.slice(start, end);

		return {
			serviceRequests: requests,
			metadata: {
				pagination
			}
		};
	}

	async getServiceRequest(params: HasServiceRequestId): Promise<ServiceRequest> {
		const req = serviceRequests.find(
			(record) => record.service_request_id === params.service_request_id
		);
		if (!req) throw new Error('Not found');

		return req;
	}
}
