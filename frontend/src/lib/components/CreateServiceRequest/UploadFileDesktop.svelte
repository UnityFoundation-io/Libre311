<script lang="ts">
	import messages from '$media/messages.json';
	import { Button } from 'stwui';
	import { FilePicker } from 'stwui';
	import { uploadIcon } from '$lib/assets/upload-icon.js';
</script>

<div class="flex h-full w-full items-center justify-center">
	<div class="flex-col">
		<div class="mb-4">
			<FilePicker onDrop={(files) => console.log(files)}>
				<FilePicker.Icon slot="icon" data={uploadIcon} />
				<FilePicker.Title slot="title">{messages['photo']['upload']}</FilePicker.Title>
				<FilePicker.Description slot="description">Drag & Drop your file</FilePicker.Description>
			</FilePicker>
		</div>

		<div class="grid grid-rows-2 gap-3">
			<Button type="primary" on:click={() => {}}>
				{messages['photo']['no_upload']}
			</Button>

			<Button type="link" on:click={() => {}}>
				{messages['photo']['back']}
			</Button>
		</div>
	</div>
</div>
