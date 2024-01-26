<script lang="ts">
	import ReviewServiceRequest from '$lib/components/ReviewServiceRequest.svelte';
	import { type ServiceRequest } from '$lib/services/Libre311/Libre311';

	let data: ServiceRequest = {
		description:
			'Bus shelter on 270 Temple St. New Haven broken glass that could cause injury to a person. ',
		address: '270 Temple St New Haven, CT, 06511, USA',
		lat: '41.3083092093462',
		service_request_id: 2,
		requested_datetime: '2023-03-27T15:05:50-04:00',
		long: '-72.9258607025516',
		service_code: '22112',
		status: 'Open',
		zipcode: '06511',
		updated_datetime: '2023-03-30T11:38:16-04:00',
		agency_responsible: 'City of New Haven',
		service_name: 'Bus Shelter Repair',
		media_url:
			'https://seeclickfix.com/rails/active_storage/representations/redirect/eyJfcmFpbHMiOnsibWVzc2FnZSI6IkJBaHBBMDJycmc9PSIsImV4cCI6bnVsbCwicHVyIjoiYmxvYl9pZCJ9fQ==--0e11181caadcd8da758f96b7196a69f8d20f5388/eyJfcmFpbHMiOnsibWVzc2FnZSI6IkJBaDdDVG9MWm05eWJXRjBTU0lJYW5CbkJqb0dSVlE2RUdGMWRHOWZiM0pwWlc1MFZEb0tjM1J5YVhCVU9ndHlaWE5wZW1WSklndzRNREI0TmpBd0Jqc0dWQT09IiwiZXhwIjpudWxsLCJwdXIiOiJ2YXJpYXRpb24ifX0=--ee2a5b821fe0552ba6053903421e37a595d06845/Temple%20St.%20&%20Elm%20St%20Bus%20Stop%20Shelter%20Window%20Damage%20(4).jpg',
		expected_datetime: '2023-03-30T11:38:16-04:00',
		address_id: null,
		service_notice: null,
		status_notes: null
	};
</script>

<ReviewServiceRequest {data} />
