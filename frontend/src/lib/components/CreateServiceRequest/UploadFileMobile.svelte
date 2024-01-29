<script lang="ts">
	import messages from '$media/messages.json';
	import { Button } from 'stwui';
</script>

<div class="flex h-full w-full items-center justify-center">
	<div class="flex-col">
		<div class="grid grid-rows-2 gap-3">
			<Button type="primary" on:click={() => {}}>
				{messages['photo']['take_photo']}
			</Button>

			<Button type="primary" on:click={() => {}}>
				{messages['photo']['camera_roll']}
			</Button>

			<Button type="link" on:click={() => {}}>
				{messages['photo']['no_upload']}
			</Button>

			<Button type="link" on:click={() => {}}>
				{messages['photo']['back']}
			</Button>
		</div>
	</div>
</div>
