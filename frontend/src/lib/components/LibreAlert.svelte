<script lang="ts">

	import { informationCircle } from './Svg/solid/informationCircle';
	import { exclamationTriangle } from './Svg/solid/exclamationTriangle';
	import { exclamationCircle } from './Svg/solid/exclamationCircle';
	import { ALERT_ANIMATION_DURATION, type AlertType } from '$lib/context/Libre311AlertStore';
	import { createEventDispatcher } from 'svelte';
	import { fly } from 'svelte/transition';
	import { checkCircle } from './Svg/solid/checkCircle';
    import {Button} from "$lib/components/ui/button";
    import * as Alert from "$lib/components/ui/alert";

	const dispatch = createEventDispatcher<{ close: void }>();

	const typeIconMap = {
		error: exclamationCircle,
		info: informationCircle,
		warn: exclamationTriangle,
		success: checkCircle
	};

	interface Props {
		type: AlertType;
		title: string;
		description: string | undefined;
	}

	let { type, title, description }: Props = $props();
</script>

<div
	in:fly|global={{ y: 200, duration: ALERT_ANIMATION_DURATION }}
	out:fly|global={{ x: 200, duration: ALERT_ANIMATION_DURATION }}
>
	<Alert.Root>
                {typeIconMap[type]}
				<Alert.Title >{title}</Alert.Title>

				<Alert.Description >
				<!-- todo change back to @html once csp policy is corrected -> https://kit.svelte.dev/docs/configuration#csp -->
				<!-- {@html description} -->
				{description}
			</Alert.Description>

				<Button on:click={() => dispatch('close')} class="ml-3"  variant="ghost">Close</Button>
	</Alert.Root>
</div>

<style>
	div {
		position: fixed;
		left: 50%;
		bottom: 20px;
		transform: translate(-50%, -50%);
		margin: 0 auto;
		min-width: 15rem;
	}
</style>
