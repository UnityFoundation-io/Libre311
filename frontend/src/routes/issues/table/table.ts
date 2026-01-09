import type { TableColumn } from 'stwui/types';
import messages from '$media/messages.json';

// Responsive widths to ensure 100% total at each breakpoint
export const columns: TableColumn[] = [
	{
		column: 'service_request_id',
		label: messages['serviceRequest']['request_id'],
		placement: 'center',
		class: 'w-[15%] lg:w-[10%] xl:w-[8%]'
	},
	{
		column: 'priority',
		label: messages['serviceRequest']['priority'],
		placement: 'center',
		class: 'hidden lg:table-cell lg:w-[10%] xl:w-[8%]'
	},
	{
		column: 'service_name',
		label: messages['serviceRequest']['service_name'],
		placement: 'center',
		class: 'w-[45%] lg:w-[30%] xl:w-[20%]'
	},
	{
		column: 'status',
		label: messages['serviceRequest']['status'],
		placement: 'center',
		class: 'w-[20%] lg:w-[15%] xl:w-[10%]'
	},
	{
		column: 'address',
		label: messages['serviceRequest']['address'],
		placement: 'center',
		class: 'hidden xl:table-cell xl:w-[22%]'
	},
	{
		column: 'reported_at',
		label: 'Reported',
		placement: 'center',
		class: 'hidden lg:table-cell lg:w-[20%] xl:w-[12%]'
	},
	{
		column: 'expected_completion',
		label: messages['serviceRequest']['expected_datetime'],
		placement: 'center',
		class: 'hidden xl:table-cell xl:w-[12%]'
	},
    {
        column: 'removal_suggestions_count',
        label: 'Removal Suggestions',
        placement: 'center',
        class: 'w-[20%] lg:w-[15%] xl:w-[8%]'
    }
];
