import type { TableColumn } from 'stwui/types';

// 14% * 7 = 98%
export const columns: TableColumn[] = [
	{
		column: 'service_request_id',
		label: 'Request ID',
		placement: 'center',
		class: 'w-[14%]'
	},
	{
		column: 'priority',
		label: 'Priority',
		placement: 'center',
		class: 'hidden lg:table-cell w-[14%]'
	},
	{
		column: 'service_name',
		label: 'Service Name',
		placement: 'center',
		class: 'w-[14%]'
	},
	{
		column: 'status',
		label: 'Status',
		placement: 'center',
		class: 'w-[14%]'
	},
	{
		column: 'address',
		label: 'Address',
		placement: 'center',
		class: 'hidden xl:table-cell w-[14%]'
	},
	{
		column: 'reported_at',
		label: 'Reported',
		placement: 'center',
		class: 'hidden lg:table-cell w-[14%]'
	},
	{
		column: 'expected_completion',
		label: 'Expected Completion',
		placement: 'center',
		class: 'hidden xl:table-cell w-[14%]'
	}
];
