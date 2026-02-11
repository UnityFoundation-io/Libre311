import type {
	CreateGroupParams,
	CreateRemovalSuggestionParams,
	CreateServiceDefinitionAttributeResponse,
	CreateServiceDefinitionAttributesParams,
	CreateServiceParams,
	CreateServiceRequestParams,
	CreateServiceRequestResponse,
	CreateServiceResponse,
	EditGroupParams,
	EditServiceDefinitionAttributeParams,
	EditServiceDefinitionAttributeResponse,
	EditServiceParams,
	FilteredServiceRequestsParams,
	GetGroupListResponse,
	GetRemovalSuggestionsResponse,
	GetServiceListResponse,
	Group,
	HasGroupId,

	HasServiceCode,
	HasServiceRequestId,
	JurisdictionConfig,
	Libre311Service,
	Service,
	ServiceDefinition,
	ServiceRequest,
	ServiceRequestsResponse
} from './Libre311';
import type { DeleteServiceRequestRequest } from './types/DeleteServiceRequestRequest';
import type { ReverseGeocodeResponse } from '../geocoding';
import type { UnityAuthLoginResponse } from '../UnityAuth/UnityAuth';
import type { UpdateSensitiveServiceRequestRequest } from './types/UpdateSensitiveServiceRequest';
import type { NetworkStatus } from '../NetworkStatus';
import type { OfflineQueue } from '../OfflineQueue';

export class OfflineAwareLibre311Service implements Libre311Service {
	constructor(
		private wrapped: Libre311Service,
		private networkStatus: NetworkStatus,
		private offlineQueue: OfflineQueue
	) {}

	async createServiceRequest(
		params: CreateServiceRequestParams
	): Promise<CreateServiceRequestResponse> {
		// Always attach a client_request_id for idempotency
		const paramsWithId: CreateServiceRequestParams = {
			...params,
			client_request_id: params.client_request_id ?? crypto.randomUUID()
		};

		if (this.networkStatus.online) {
			return this.wrapped.createServiceRequest(paramsWithId);
		}

		// Offline: queue the request with photo blob if available
		const serviceName = params._serviceName ?? '';
		const photoFile = params._photoFile;

		await this.offlineQueue.enqueue(paramsWithId, serviceName, photoFile);

		return {
			service_request_id: -1,
			service_notice: 'Your request has been saved and will be submitted when you are back online.'
		};
	}

	async uploadImage(file: File): Promise<string> {
		if (this.networkStatus.online) {
			return this.wrapped.uploadImage(file);
		}
		// Return placeholder; the actual file will be passed via _photoFile in createServiceRequest
		return 'offline://pending';
	}

	// All other methods delegate directly
	getJurisdictionConfig(): JurisdictionConfig {
		return this.wrapped.getJurisdictionConfig();
	}

	reverseGeocode(coords: L.PointTuple): Promise<ReverseGeocodeResponse> {
		return this.wrapped.reverseGeocode(coords);
	}

	setAuthInfo(authInfo: UnityAuthLoginResponse | undefined): void {
		this.wrapped.setAuthInfo(authInfo);
	}

	getServiceList(): Promise<GetServiceListResponse> {
		return this.wrapped.getServiceList();
	}

	getServiceDefinition(params: HasServiceCode): Promise<ServiceDefinition> {
		return this.wrapped.getServiceDefinition(params);
	}

	getServiceRequests(params: FilteredServiceRequestsParams): Promise<ServiceRequestsResponse> {
		return this.wrapped.getServiceRequests(params);
	}

	getServiceRequest(params: HasServiceRequestId): Promise<ServiceRequest> {
		return this.wrapped.getServiceRequest(params);
	}

	getGroupList(): Promise<GetGroupListResponse> {
		return this.wrapped.getGroupList();
	}

	createGroup(params: CreateGroupParams): Promise<Group> {
		return this.wrapped.createGroup(params);
	}

	editGroup(params: EditGroupParams): Promise<Group> {
		return this.wrapped.editGroup(params);
	}

	deleteGroup(params: HasGroupId): Promise<void> {
		return this.wrapped.deleteGroup(params);
	}

	downloadServiceRequests(params: FilteredServiceRequestsParams): Promise<Blob> {
		return this.wrapped.downloadServiceRequests(params);
	}

	createService(params: CreateServiceParams): Promise<CreateServiceResponse> {
		return this.wrapped.createService(params);
	}

	updateServicesOrder(
		params: Parameters<Libre311Service['updateServicesOrder']>[0]
	): Promise<GetServiceListResponse> {
		return this.wrapped.updateServicesOrder(params);
	}

	createAttribute(
		params: CreateServiceDefinitionAttributesParams
	): Promise<CreateServiceDefinitionAttributeResponse> {
		return this.wrapped.createAttribute(params);
	}

	editAttribute(
		params: EditServiceDefinitionAttributeParams
	): Promise<EditServiceDefinitionAttributeResponse> {
		return this.wrapped.editAttribute(params);
	}

	deleteAttribute(params: Parameters<Libre311Service['deleteAttribute']>[0]): Promise<void> {
		return this.wrapped.deleteAttribute(params);
	}

	updateAttributesOrder(
		params: Parameters<Libre311Service['updateAttributesOrder']>[0]
	): Promise<ServiceDefinition> {
		return this.wrapped.updateAttributesOrder(params);
	}

	editService(params: EditServiceParams): Promise<Service> {
		return this.wrapped.editService(params);
	}

	deleteService(params: HasServiceCode): Promise<void> {
		return this.wrapped.deleteService(params);
	}

	updateServiceRequest(params: UpdateSensitiveServiceRequestRequest): Promise<ServiceRequest> {
		return this.wrapped.updateServiceRequest(params);
	}

	deleteServiceRequest(params: DeleteServiceRequestRequest): Promise<boolean> {
		return this.wrapped.deleteServiceRequest(params);
	}

	createRemovalSuggestion(params: CreateRemovalSuggestionParams): Promise<void> {
		return this.wrapped.createRemovalSuggestion(params);
	}

	getRemovalSuggestions(
		service_request_id: number
	): Promise<GetRemovalSuggestionsResponse> {
		return this.wrapped.getRemovalSuggestions(service_request_id);
	}

	deleteRemovalSuggestion(params: { id: number }): Promise<void> {
		return this.wrapped.deleteRemovalSuggestion(params);
	}
}
