<script lang="ts">
	import { List } from 'stwui';
</script>

<List.Item class="flex cursor-pointer items-center hover:bg-gray-100">
	<slot name="leading" />
	<div class="mx-4 w-full">
		<slot name="main" />
	</div>

	<div class="flex justify-end">
		<div class="mx-2 flex items-center justify-center">
			<slot name="trailing" />
		</div>
	</div>
</List.Item>
