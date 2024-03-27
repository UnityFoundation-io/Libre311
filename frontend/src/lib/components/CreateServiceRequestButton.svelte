<script>
	import { Button } from 'stwui';

	import PlusCircleIcon from './Svg/outline/PlusCircleIcon.svelte';
</script>

<div class="absolute bottom-5 left-1/2 z-[1] -translate-x-1/2">
	<Button type="primary" href={'/issue/create'}>
		<div slot="leading">
			<PlusCircleIcon />
		</div>
		New Request
	</Button>
</div>
