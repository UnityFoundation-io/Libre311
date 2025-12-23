import { goto } from '$app/navigation';
import { FilteredServiceRequestsParamsMapper } from '$lib/services/Libre311/FilteredServiceRequestsParamsMapper';
import {
	type Libre311Service,
	type ServiceRequest,
	type ServiceRequestsResponse,
	EMPTY_PAGINATION,
	type FilteredServiceRequestsParams
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
	applyServiceRequestParams(params: FilteredServiceRequestsParams, url: URL): void;
	refreshSelectedServiceRequest(updatedServiceRequest: ServiceRequest): void;
};

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
			// User navigated directly to a detail page (e.g., page refresh)
			// Fetch filtered service requests to populate the table, then select the specific one
			try {
				const updatedParams = FilteredServiceRequestsParamsMapper.toRequestParams(
					page.url.searchParams
				);
				const res = await libreService.getServiceRequests(updatedParams);
				serviceRequestsResponse.set(asAsyncSuccess(res));

				// Find and select the specific service request from the list
				const selected = res.serviceRequests.find(
					(req) => req.service_request_id === +page.params.issue_id
				);

				if (selected) {
					selectedServiceRequest.set(selected);
				} else {
					// Request not in filtered results - fetch it individually and add to list
					const singleRequest = await libreService.getServiceRequest({
						service_request_id: +page.params.issue_id
					});
					selectedServiceRequest.set(singleRequest);
				}
			} catch (error) {
				serviceRequestsResponse.set(asAsyncFailure(error));
			}
		}
	}

	// state updates for when  user navigates to /issues/map
	async function handleMapPageNav(page: Page<Record<string, string>, string | null>) {
		try {
			selectedServiceRequest.set(undefined);
			const updatedParams = FilteredServiceRequestsParamsMapper.toRequestParams(
				page.url.searchParams
			);
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
		if (page.route.id?.endsWith('[issue_id]')) {
			await handleIssueDetailsPageNav(page);
		} else if (page.route.id?.startsWith('/issues')) {
			await handleMapPageNav(page);
		}
	});

	function applyServiceRequestParams(params: FilteredServiceRequestsParams, url: URL) {
		const queryParams = FilteredServiceRequestsParamsMapper.toURLSearchParams(params);
		goto(`${url.pathname}?${queryParams.toString()}`);
	}

	function refreshSelectedServiceRequest(updatedServiceRequest: ServiceRequest) {
		selectedServiceRequest.set(updatedServiceRequest);

		const currentResponse = get(serviceRequestsResponse);
		if (currentResponse.type === 'success') {
			const updatedServiceRequests = currentResponse.value.serviceRequests.map((req) =>
				req.service_request_id === updatedServiceRequest.service_request_id
					? updatedServiceRequest
					: req
			);
			serviceRequestsResponse.set(
				asAsyncSuccess({
					...currentResponse.value,
					serviceRequests: updatedServiceRequests
				})
			);
		}
	}

	const ctx: ServiceRequestsContext = {
		selectedServiceRequest,
		serviceRequestsResponse,
		applyServiceRequestParams,
		refreshSelectedServiceRequest
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
