<script lang="ts">
	import messages from '$media/messages.json';
	import { Button } from 'stwui';
</script>

<div class="flex h-full w-full items-center justify-center">
	<div class="flex-col">
		<div class="grid grid-rows-2 gap-3">
			<input type="file" id="actual-btn" accept="image/*" capture="environment" hidden />
			<label for="actual-btn">{messages['photo']['take_photo']}</label>

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

<style>
	label {
		background-color: hsl(var(--primary));
		color: hsl(var(--primary-content));
		padding: 0.5rem;
		font-family: sans-serif;
		text-align: center;
		border-radius: 0.3rem;
		cursor: pointer;
		margin-top: 1rem;
	}
</style>
