<script>
	import { Button } from '$lib/components/ui/button';

	import PlusCircleIcon from './Svg/outline/PlusCircleIcon.svelte';
</script>

<div class="absolute bottom-5 left-1/2 z-[1] -translate-x-1/2">
	<Button variant="default" href={'/issue/create'}>
				<div >
				<PlusCircleIcon />
			</div>
		New Request
	</Button>
</div>
