import type {
	GetServiceRequestsParams,
	Libre311Service,
	Pagination,
	ServiceRequest
} from '$lib/services/Libre311/Libre311';
import { ASYNC_IN_PROGRESS, asAsyncSuccess, type AsyncResult } from '$lib/services/http';
import type { Maybe } from '$lib/utils/types';
import type { Page } from '@sveltejs/kit';
import { getContext, setContext } from 'svelte';
import { writable, type Readable } from 'svelte/store';

const key = Symbol();
const issuesBasePath = '/issues/map';

export type ServiceRequestsContext = {
	nextPageLink: Readable<Maybe<string>>;
	prevPageLink: Readable<Maybe<string>>;
	issuesLink: Readable<string>;
	serviceRequests: Readable<AsyncResult<ServiceRequest[]>>;
	selectedServiceRequest: Readable<Maybe<ServiceRequest>>;
	// todo mapBounds store
	// todo applyFilters function
	// will need to have applyFilters function to filter data (this will reset the pagination) (takes filterable values and updates the URLSearchParams accordingly)
	// 		will be Set<ServiceId> or something of to that effect.
};

function toServiceRequestParams(searchParams: URLSearchParams) {
	const params: GetServiceRequestsParams = {};
	if (searchParams.get('pageNumber')) params.pageNumber = Number(searchParams.get('pageNumber'));

	if (searchParams.get('serviceCode'))
		params.serviceCode = searchParams.get('serviceCode') ?? undefined;
	if (searchParams.get('startDate')) params.startDate = searchParams.get('startDate') ?? undefined;
	if (searchParams.get('endDate')) params.endDate = searchParams.get('endDate') ?? undefined;
	// todo validate format
	const maybeStatusString = searchParams.get('status');
	if (maybeStatusString) params.status = JSON.parse(maybeStatusString);

	return params;
}

function toURLSearchParams(srParams: GetServiceRequestsParams) {
	const searchParams = new URLSearchParams();
	if (srParams.pageNumber) searchParams.append('pageNumber', srParams.pageNumber.toString());
	if (srParams.serviceCode) searchParams.append('serviceCode', srParams.serviceCode);
	if (srParams.startDate) searchParams.append('startDate', srParams.startDate);
	if (srParams.endDate) searchParams.append('endDate', srParams.endDate);
	// todo validate that this is the format the backend expects for a list type query param
	if (srParams.status) searchParams.append('status', "[${srParams.status.join(', ')}]");

	return searchParams;
}

function createNextPageLink(pagination: Pagination, searchParams: URLSearchParams) {
	if (pagination.totalPages === pagination.pageNumber) return;
	searchParams.append('pageNumber', (pagination.pageNumber + 1).toString());
	return issuesBasePath + '?' + searchParams.toString();
}

function createPrevPageLink(pagination: Pagination, searchParams: URLSearchParams) {
	if (pagination.pageNumber === 0) {
		return;
	}
	searchParams.append('pageNumber', (pagination.pageNumber - 1).toString());
	return issuesBasePath + '?' + searchParams.toString();
}

function createIssuesLink(pagination: Pagination, searchParams: URLSearchParams) {
	if (searchParams.entries().next().done) {
		return issuesBasePath;
	}
	searchParams.append('pageNumber', pagination.pageNumber.toString());
	return issuesBasePath + '?' + searchParams.toString();
}

export function createServiceRequestsContext(
	libreService: Libre311Service,
	page: Readable<Page<Record<string, string>, string | null>>
): ServiceRequestsContext {
	const nextPageLink = writable<Maybe<string>>();
	const prevPageLink = writable<Maybe<string>>();
	const issuesLink = writable<string>(issuesBasePath);
	const serviceRequests = writable<AsyncResult<ServiceRequest[]>>(ASYNC_IN_PROGRESS);
	const selectedServiceRequest = writable<Maybe<ServiceRequest>>();

	page.subscribe(async (page: Page<Record<string, string>, string | null>) => {
		if (page.params.issue_id) {
			// set the selectedServiceRequest
			// then return, preventing map data updates and link updates from occurring when in 'details' view
			return;
		}

		const updatedParams = toServiceRequestParams(page.url.searchParams);

		const res = await libreService.getServiceRequests(updatedParams);
		serviceRequests.set(asAsyncSuccess(res.serviceRequests));

		// likely don't need to do this conversion
		const updatedURlSearchParams = toURLSearchParams(updatedParams);
		issuesLink.set(createIssuesLink(res.metadata.pagination, updatedURlSearchParams));
		prevPageLink.set(createPrevPageLink(res.metadata.pagination, updatedURlSearchParams));
		nextPageLink.set(createNextPageLink(res.metadata.pagination, updatedURlSearchParams));
	});

	const ctx: ServiceRequestsContext = {
		nextPageLink,
		prevPageLink,
		issuesLink,
		serviceRequests,
		selectedServiceRequest
	};

	setContext(key, ctx);

	return ctx;
}

export function useServiceRequestsContext(): ServiceRequestsContext {
	return getContext<ServiceRequestsContext>(key);
}
