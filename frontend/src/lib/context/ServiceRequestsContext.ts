import {
	type ServiceRequestId,
	type GetServiceRequestsParams,
	type Libre311Service,
	type Pagination,
	type ServiceRequest
} from '$lib/services/Libre311/Libre311';
import {
	asAsyncFailure,
	asAsyncSuccess,
	ASYNC_IN_PROGRESS,
	type AsyncResult
} from '$lib/services/http';

import type { Maybe } from '$lib/utils/types';
import type { Page } from '@sveltejs/kit';
import { getContext, setContext } from 'svelte';
import { writable, type Readable, derived, get } from 'svelte/store';

const key = Symbol();
const issuesBasePath = '/issues/map';

export type ServiceRequestsContext = {
	pagination: Readable<EnhancedPagination>;
	issuesLink: Readable<string>;
	serviceRequests: Readable<AsyncResult<ServiceRequest[]>>;
	selectedServiceRequest: Readable<Maybe<ServiceRequest>>;
	// todo mapBounds store (map should re center whenever new serviceRequests are loaded)
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

function createNextPageLink(
	pagination: Pagination,
	searchParams: URLSearchParams,
	issuesBasePath: string | null
) {
	if (pagination.totalPages === pagination.pageNumber + 1) return;
	searchParams.set('pageNumber', (pagination.pageNumber + 1).toString());
	return issuesBasePath + '?' + searchParams.toString();
}

function createPrevPageLink(
	pagination: Pagination,
	searchParams: URLSearchParams,
	issuesBasePath: string | null
) {
	if (pagination.pageNumber === 0) {
		return;
	}
	searchParams.set('pageNumber', (pagination.pageNumber - 1).toString());
	return issuesBasePath + '?' + searchParams.toString();
}

function createIssuesLink(searchParams: URLSearchParams) {
	if (searchParams.size == 0) {
		return issuesBasePath;
	}

	return issuesBasePath + '?' + searchParams.toString();
}

export type EnhancedPagination = Pagination & {
	nextPage?: string;
	prevPage?: string;
};

export function createServiceRequestsContext(
	libreService: Libre311Service,
	page: Readable<Page<Record<string, string>, string | null>>
): ServiceRequestsContext {
	const pagination = writable<EnhancedPagination>();
	const issuesLink = writable<string>(issuesBasePath);
	// consider type as Maybe<AsyncRequest<ServiceRequest>> that would cover all possible states
	const selectedServiceRequest = writable<Maybe<ServiceRequest>>();
	const serviceRequestsMapStore =
		writable<AsyncResult<Map<ServiceRequestId, ServiceRequest>>>(ASYNC_IN_PROGRESS);

	// state updates to be done when a user navigates to /issues/map/[issue_id]
	async function handleIssueDetailsPageNav(page: Page<Record<string, string>, string | null>) {
		const asyncMapStoreVal = get(serviceRequestsMapStore);
		if (asyncMapStoreVal.type === 'success') {
			// data has already been loaded, update our selectedServiceRequest to the value
			// later on we may way to request the record to ensure it is completely up to date
			selectedServiceRequest.set(asyncMapStoreVal.value.get(+page.params.issue_id));
		} else {
			// this covers a bit of an edge case where the user navivated straight to the page and we don't have any incidents loaded
			// so we need to fetch the specific ServiceRequest, add it to our serviceRequestsMapStore, and update the serviceRequests store.
			try {
				const res = await libreService.getServiceRequest({
					service_request_id: +page.params.issue_id
				});
				selectedServiceRequest.set(res);
				const initialMap = new Map<ServiceRequestId, ServiceRequest>();
				initialMap.set(res.service_request_id, res);
				serviceRequestsMapStore.set(asAsyncSuccess(initialMap));
			} catch (error) {
				serviceRequestsMapStore.set(asAsyncFailure(error));
			}
		}
	}

	// state updates for when  user navigates to /issues/map
	async function handleMapPageNav(page: Page<Record<string, string>, string | null>) {
		try {
			selectedServiceRequest.set(undefined);
			const updatedParams = toServiceRequestParams(page.url.searchParams);
			const res = await libreService.getServiceRequests(updatedParams);
			serviceRequestsMapStore.set(
				asAsyncSuccess(new Map(res.serviceRequests.map((res) => [res.service_request_id, res])))
			);

			issuesLink.set(createIssuesLink(page.url.searchParams));
			pagination.set({
				...res.metadata.pagination,
				nextPage: createNextPageLink(res.metadata.pagination, page.url.searchParams, page.route.id),
				prevPage: createPrevPageLink(res.metadata.pagination, page.url.searchParams, page.route.id)
			});
		} catch (error) {
			serviceRequestsMapStore.set({
				type: 'failure',
				error
			});
		}
	}

	page.subscribe(async (page: Page<Record<string, string>, string | null>) => {
		if (page.route.id === '/issues/map/[issue_id]') {
			await handleIssueDetailsPageNav(page);
		} else if (page.route.id === '/issues/map' || page.route.id === '/issues/list') {
			await handleMapPageNav(page);
		}
	});

	const serviceRequests: Readable<AsyncResult<ServiceRequest[]>> = derived(
		serviceRequestsMapStore,
		(asyncResultMap: AsyncResult<Map<ServiceRequestId, ServiceRequest>>) => {
			if (asyncResultMap.type === 'success') {
				return asAsyncSuccess(Array.from(asyncResultMap.value.values()));
			} else return asyncResultMap;
		}
	);

	const ctx: ServiceRequestsContext = {
		pagination,
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

export function useServiceRequestsStore(): ServiceRequestsContext['serviceRequests'] {
	return useServiceRequestsContext().serviceRequests;
}

export function useSelectedServiceRequestStore(): ServiceRequestsContext['selectedServiceRequest'] {
	return useServiceRequestsContext().selectedServiceRequest;
}

export function useIssuesLinkStore(): ServiceRequestsContext['issuesLink'] {
	return useServiceRequestsContext().issuesLink;
}

export function usePaginationStore(): ServiceRequestsContext['pagination'] {
	return useServiceRequestsContext().pagination;
}
