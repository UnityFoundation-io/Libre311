<script lang='ts'>
	import image from '$lib/assets/cracked-road.png';
	import messages from '$media/messages.json'
	import { goto } from '$app/navigation';
	import { Badge } from 'stwui';

	// Test Data
	let myData = {
		'title': 'Road Issue',
		'address': '220 N Main St, Paris MO 65275',
		'description': 'It looks like a light clog has formed at this intersection',
		'requested_datetime': '2023-03-29T14:36:43-04:00',
		'service_request_id': '14282821',
		'status': 'Acknowledged'
	}

	const gotoServiceRequest = () => {
		goto('/issues/map/123');
	}
</script>

<div class='flow-root'>
	<h1 class='text-lg float-left tracking-wide'>#{myData.service_request_id}</h1>
	<Badge class='text-sm float-right' type='warn'>{myData.status}</Badge>
</div>

<p class='text-sm font-extralight'>{myData.requested_datetime}</p>

<button on:click={gotoServiceRequest}>
	<img class='rounded-md' alt='service-request' src={image}/>
</button>
<div>

</div>

<a class="text-sm underline underline-offset-1" href='http://tinyurl.com/2hntej99'>{myData.address}</a>

<div>
	<strong class="text-base">{messages['serviceRequest']['description']}</strong>
	<p class='w-82 overflow-hidden text-sm text-ellipsis whitespace-nowrap'>{myData.description}</p>
</div>
