import type { TableColumn } from 'stwui/types';
import messages from '$media/messages.json';

// 14% * 7 = 98%
export const columns: TableColumn[] = [
	{
		column: 'service_request_id',
		label: messages['serviceRequest']['request_id'],
		placement: 'center',
		class: 'w-[14%]'
	},
	{
		column: 'priority',
		label: messages['serviceRequest']['priority'],
		placement: 'center',
		class: 'hidden lg:table-cell w-[14%]'
	},
	{
		column: 'service_name',
		label: messages['serviceRequest']['service_name'],
		placement: 'center',
		class: 'w-[14%]'
	},
	{
		column: 'status',
		label: messages['serviceRequest']['status'],
		placement: 'center',
		class: 'w-[14%]'
	},
	{
		column: 'address',
		label: messages['serviceRequest']['address'],
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
		label: messages['serviceRequest']['expected_datetime'],
		placement: 'center',
		class: 'hidden xl:table-cell w-[14%]'
	}
];
