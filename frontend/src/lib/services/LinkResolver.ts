import type { CreateServiceRequestSteps } from '$lib/components/CreateServiceRequest/shared';
import type { Pagination, ServiceRequestId } from './Libre311/Libre311';

export class LinkResolver {
	private createIssueLink(basePath: string, url: URL): string {
		if (!url.pathname.startsWith('/issues') || url.searchParams.size === 0) return basePath;
		const params = this.copySearchParams(url.searchParams);
		if (basePath.includes('/project/')) {
			params.delete('project_slug');
		}
		if (params.size === 0) return basePath;
		return basePath + '?' + params.toString();
	}

	private copySearchParams(searchParams: URLSearchParams) {
		return new URLSearchParams(searchParams.toString());
	}

	private getProjectSlug(url: URL): string | undefined {
		// 1. Check URL search params (preferred)
		if (url.searchParams.has('project_slug')) {
			return url.searchParams.get('project_slug') ?? undefined;
		}
		// 2. Check window.location search params (fallback)
		if (typeof window !== 'undefined' && window.location.search.includes('project_slug')) {
			return new URLSearchParams(window.location.search).get('project_slug') ?? undefined;
		}
		// 3. Check pathname (e.g., /issues/map/project/my-project)
		const projectMatch = url.pathname.match(/\/project\/([^\/]+)/);
		if (projectMatch) {
			return projectMatch[1];
		}

		return undefined;
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

	nextIssuesTablePage(pagination: Pagination, url: URL) {
		const searchParams = this.copySearchParams(url.searchParams);
		if (pagination.totalPages === pagination.pageNumber + 1) return;
		searchParams.set('pageNumber', (pagination.pageNumber + 1).toString());
		return '/issues/table?' + searchParams.toString();
	}

	prevIssuesTablePage(pagination: Pagination, url: URL) {
		if (pagination.pageNumber === 0) {
			return;
		}
		const searchParams = this.copySearchParams(url.searchParams);
		searchParams.set('pageNumber', (pagination.pageNumber - 1).toString());
		return '/issues/table?' + searchParams.toString();
	}

	issuesMap(url: URL) {
		const projectSlug = this.getProjectSlug(url);
		if (projectSlug) {
			return this.createIssueLink(`/issues/map/project/${projectSlug}`, url);
		}
		return this.createIssueLink('/issues/map', url);
	}

	issuesList(url: URL) {
		const projectSlug = this.getProjectSlug(url);
		const basePath = projectSlug ? `/issues/list/project/${projectSlug}` : '/issues/list';
		return this.createIssueLink(basePath, url);
	}

	issuesTable(url: URL) {
		const projectSlug = this.getProjectSlug(url);
		if (projectSlug) {
			const searchParams = this.copySearchParams(url.searchParams);
			searchParams.set('project_slug', projectSlug);
			const newUrl = new URL(url.toString());
			newUrl.search = searchParams.toString();
			return this.createIssueLink('/issues/table', newUrl);
		}
		return this.createIssueLink('/issues/table', url);
	}

	issueCreate(url: URL) {
		const projectSlug = this.getProjectSlug(url);
		if (projectSlug) {
			return `/issue/create?project_slug=${projectSlug}`;
		}
		return '/issue/create';
	}

	issueDetailsDesktop(url: URL, id: ServiceRequestId) {
		const projectSlug = this.getProjectSlug(url);
		if (projectSlug) {
			const searchParams = this.copySearchParams(url.searchParams);
			searchParams.set('project_slug', projectSlug);
			const newUrl = new URL(url.toString());
			newUrl.search = searchParams.toString();
			return this.createIssueLink(`/issues/map/${id}`, newUrl);
		}
		return this.createIssueLink(`/issues/map/${id}`, url);
	}
	issueDetailsMobile(url: URL, id: ServiceRequestId) {
		const projectSlug = this.getProjectSlug(url);
		if (projectSlug) {
			const searchParams = this.copySearchParams(url.searchParams);
			searchParams.set('project_slug', projectSlug);
			const newUrl = new URL(url.toString());
			newUrl.search = searchParams.toString();
			return this.createIssueLink(`/issues/list/${id}`, newUrl);
		}
		return this.createIssueLink(`/issues/list/${id}`, url);
	}
	issueDetailsTable(url: URL, id: ServiceRequestId) {
		const projectSlug = this.getProjectSlug(url);
		if (projectSlug) {
			const searchParams = this.copySearchParams(url.searchParams);
			searchParams.set('project_slug', projectSlug);
			const newUrl = new URL(url.toString());
			newUrl.search = searchParams.toString();
			return this.createIssueLink(`/issues/table/${id}`, newUrl);
		}
		return this.createIssueLink(`/issues/table/${id}`, url);
	}

	createIssuePageGetCurrentStep(url: URL): CreateServiceRequestSteps {
		return Number(url.searchParams.get('step')) ?? 0;
	}

	createIssuePageNext(url: URL) {
		let currentStep = this.createIssuePageGetCurrentStep(url);
		const searchParams = this.copySearchParams(url.searchParams);
		searchParams.set('step', (++currentStep).toString());
		return `/issue/create?${searchParams.toString()}`;
	}
	createIssuePagePrevious(url: URL) {
		let currentStep = this.createIssuePageGetCurrentStep(url);
		const searchParams = this.copySearchParams(url.searchParams);
		searchParams.set('step', (--currentStep).toString());
		return `/issue/create?${searchParams.toString()}`;
	}
}
