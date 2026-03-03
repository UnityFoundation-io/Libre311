import { describe, it, expect } from 'vitest';
import { FilteredServiceRequestsParamsMapper } from './FilteredServiceRequestsParamsMapper';
import type { FilteredServiceRequestsParams } from './Libre311';

describe('FilteredServiceRequestsParamsMapper', () => {
	describe('toURLSearchParams', () => {
		it('should include project_id when provided', () => {
			const params: FilteredServiceRequestsParams = {
				project_id: 123,
				status: ['open'],
				servicePriority: ['low']
			};
			const searchParams = FilteredServiceRequestsParamsMapper.toURLSearchParams(params);
			expect(searchParams.get('project_id')).toBe('123');
			expect(searchParams.get('status')).toBe('open');
			expect(searchParams.get('priority')).toBe('low');
		});

		it('should handle empty params', () => {
			const searchParams = FilteredServiceRequestsParamsMapper.toURLSearchParams({});
			expect(searchParams.toString()).toBe('page_size=10&page=0');
		});
	});

	describe('toRequestParams', () => {
		it('should parse project_id from URLSearchParams', () => {
			const searchParams = new URLSearchParams();
			searchParams.append('project_id', '456');
			searchParams.append('status', 'open,closed');

			const params = FilteredServiceRequestsParamsMapper.toRequestParams(searchParams);
			if (!Array.isArray(params)) {
				expect(params.project_id).toBe(456);
				expect(params.status).toEqual(['open', 'closed']);
			} else {
				throw new Error('Expected object, got array');
			}
		});

		it('should ignore invalid project_id', () => {
			const searchParams = new URLSearchParams();
			searchParams.append('project_id', 'not-a-number');
			const params = FilteredServiceRequestsParamsMapper.toRequestParams(searchParams);
			if (!Array.isArray(params)) {
				expect(params.project_id).toBeNaN();
			} else {
				throw new Error('Expected object, got array');
			}
		});
	});
});
