import {
	isServiceRequestPriority,
	isServiceRequestStatus,
	type FilteredServiceRequestsParams
} from './Libre311';

export class FilteredServiceRequestsParamsMapper {
	public static toURLSearchParams(params: FilteredServiceRequestsParams) {
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

	public static toRequestParams(searchParams: URLSearchParams): FilteredServiceRequestsParams {
		let params: FilteredServiceRequestsParams = {};

		if (searchParams.get('service_request_id')) {
			params = searchParams.get('service_request_id')?.split(',').map(Number) ?? [];
			return params;
		}

		if (searchParams.get('priority'))
			params.servicePriority =
				searchParams.get('priority')?.split(',').filter(isServiceRequestPriority) ?? undefined;

		if (searchParams.get('pageNumber')) params.pageNumber = Number(searchParams.get('pageNumber'));

		if (searchParams.get('service_code'))
			params.serviceCode = searchParams
				.get('service_code')
				?.split(',')
				.map((code) => Number(code));
		if (searchParams.get('start_date'))
			params.startDate = searchParams.get('start_date') ?? undefined;
		if (searchParams.get('end_date')) params.endDate = searchParams.get('end_date') ?? undefined;
		// todo validate format
		if (searchParams.get('status'))
			params.status =
				searchParams.get('status')?.split(',').filter(isServiceRequestStatus) ?? undefined;

		return params;
	}
}
