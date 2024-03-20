import type { CreateServiceRequestSteps } from '$lib/components/CreateServiceRequest/shared';
import type { Pagination, ServiceRequestId } from './Libre311/Libre311';

export class LinkResolver {
	private createIssueLink(basePath: string, url: URL): string {
		if (!url.pathname.startsWith('/issues' || url.searchParams.size === 0)) return basePath;
		return basePath + '?' + url.searchParams.toString();
	}

	private copySearchParams(searchParams: URLSearchParams) {
		return new URLSearchParams(searchParams.toString());
	}

	nextIssuesPage(pagination: Pagination, url: URL) {
		const searchParams = this.copySearchParams(url.searchParams);
		if (pagination.totalPages === pagination.pageNumber + 1) return;
		searchParams.set('pageNumber', (pagination.pageNumber + 1).toString());
		return url.pathname + '?' + searchParams.toString();
	}

	prevIssuesPage(pagination: Pagination, url: URL) {
		if (pagination.pageNumber === 0) {
			return;
		}
		const searchParams = this.copySearchParams(url.searchParams);
		searchParams.set('pageNumber', (pagination.pageNumber - 1).toString());
		return url.pathname + '?' + searchParams.toString();
	}

	issuesMap(url: URL) {
		return this.createIssueLink('/issues/map', url);
	}

	issuesList(url: URL) {
		return this.createIssueLink('/issues/list', url);
	}

	issuesTable(url: URL) {
		return this.createIssueLink('/issues/table', url);
	}

	issueDetailsDesktop(url: URL, id: ServiceRequestId) {
		return this.createIssueLink(`/issues/map/${id}`, url);
	}
	issueDetailsMobile(url: URL, id: ServiceRequestId) {
		return this.createIssueLink(`/issues/list/${id}`, url);
	}
	issueDetailsTable(url: URL, id: ServiceRequestId) {
		return this.createIssueLink(`/issues/table/${id}`, url);
	}

	createIssuePageGetCurrentStep(url: URL): CreateServiceRequestSteps {
		return Number(url.searchParams.get('step')) ?? 0;
	}

	createIssuePageNext(url: URL) {
		let currentStep = this.createIssuePageGetCurrentStep(url);
		return `/issue/create?step=${++currentStep}`;
	}
	createIssuePagePrevious(url: URL) {
		let currentStep = this.createIssuePageGetCurrentStep(url);
		return `/issue/create?step=${--currentStep}`;
	}
}
