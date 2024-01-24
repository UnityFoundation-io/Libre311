import {
	type GetServiceRequestsParams,
	type Libre311Service,
	type ServiceRequest,
	type ServiceRequestsResponse,
	EMPTY_PAGINATION
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
import { writable, type Readable, get } from 'svelte/store';

const key = Symbol();

export type ServiceRequestsContext = {
	selectedServiceRequest: Readable<Maybe<ServiceRequest>>;
	serviceRequestsResponse: Readable<AsyncResult<ServiceRequestsResponse>>;
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

export function createServiceRequestsContext(
	libreService: Libre311Service,
	page: Readable<Page<Record<string, string>, string | null>>
): ServiceRequestsContext {
	// consider type as Maybe<AsyncRequest<ServiceRequest>> that would cover all possible states
	const selectedServiceRequest = writable<Maybe<ServiceRequest>>();
	const serviceRequestsResponse = writable<AsyncResult<ServiceRequestsResponse>>(ASYNC_IN_PROGRESS);

	// state updates to be done when a user navigates to /issues/map/[issue_id]
	async function handleIssueDetailsPageNav(page: Page<Record<string, string>, string | null>) {
		const serviceRequestsStoreVal = get(serviceRequestsResponse);
		// data has already been loaded, update our selectedServiceRequest to the value
		if (serviceRequestsStoreVal.type === 'success') {
			const selected = serviceRequestsStoreVal.value.serviceRequests.find(
				(req) => req.service_request_id === +page.params.issue_id
			);
			// update the type for selectedServiceRequest and then we can capture this error
			if (!selected) return;

			selectedServiceRequest.set(selected);
		} else {
			// this covers a bit of an edge case where the user navigated straight to the page and we don't have any incidents loaded
			// so we need to fetch the specific ServiceRequest, add it to our serviceRequestsMapStore, and update the serviceRequests store.
			try {
				const res = await libreService.getServiceRequest({
					service_request_id: +page.params.issue_id
				});
				selectedServiceRequest.set(res);
				const asList: ServiceRequestsResponse = {
					serviceRequests: [res],
					metadata: {
						pagination: EMPTY_PAGINATION
					}
				};
				serviceRequestsResponse.set(asAsyncSuccess(asList));
			} catch (error) {
				serviceRequestsResponse.set(asAsyncFailure(error));
			}
		}
	}

	// state updates for when  user navigates to /issues/map
	async function handleMapPageNav(page: Page<Record<string, string>, string | null>) {
		try {
			selectedServiceRequest.set(undefined);
			const updatedParams = toServiceRequestParams(page.url.searchParams);
			const res = await libreService.getServiceRequests(updatedParams);
			serviceRequestsResponse.set(asAsyncSuccess(res));
		} catch (error) {
			serviceRequestsResponse.set({
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

	const ctx: ServiceRequestsContext = {
		selectedServiceRequest,
		serviceRequestsResponse
	};

	setContext(key, ctx);

	return ctx;
}

export function useServiceRequestsContext(): ServiceRequestsContext {
	return getContext<ServiceRequestsContext>(key);
}

export function useSelectedServiceRequestStore(): ServiceRequestsContext['selectedServiceRequest'] {
	return useServiceRequestsContext().selectedServiceRequest;
}

export function useServiceRequestsResponseStore(): ServiceRequestsContext['serviceRequestsResponse'] {
	return useServiceRequestsContext().serviceRequestsResponse;
}
