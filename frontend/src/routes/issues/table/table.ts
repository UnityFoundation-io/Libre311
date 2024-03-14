import type { SelectOption, TableColumn } from 'stwui/types';

// 12% * 8 = 96%
export const columns: TableColumn[] = [
	{
		column: 'service_request_id',
		label: 'Request ID',
		placement: 'center',
		class: 'w-[12%]'
	},
	{
		column: 'priority',
		label: 'Priority',
		placement: 'center',
		class: 'hidden lg:table-cell w-[12%]'
	},
	{
		column: 'service_name',
		label: 'Service Name',
		placement: 'center',
		class: 'w-[12%]'
	},
	{
		column: 'status',
		label: 'Status',
		placement: 'center',
		class: 'w-[12%]'
	},
	{
		column: 'address',
		label: 'Address',
		placement: 'center',
		class: 'hidden xl:table-cell w-[12%]'
	},
	{
		column: 'created_at',
		label: 'Created',
		placement: 'center',
		class: 'hidden lg:table-cell w-[12%]'
	},
	{
		column: 'last_updated',
		label: 'Last Updated',
		placement: 'center',
		class: 'hidden lg:table-cell w-[12%]'
	},
	{
		column: 'expected_completion',
		label: 'Expected Completion',
		placement: 'center',
		class: 'hidden xl:table-cell w-[12%]'
	}
];

export const priorityOptions: SelectOption[] = [
	{
		value: 'low',
		label: 'Low'
	},
	{
		value: 'medium',
		label: 'Medium'
	},
	{
		value: 'high',
		label: 'High'
	}
];

export const statusOptions: SelectOption[] = [
	{
		value: 'open',
		label: 'Open'
	},
	{
		value: 'closed',
		label: 'Closed'
	}
];
