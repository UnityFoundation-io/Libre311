<script lang="ts">
	import { createLibre311Context, type Libre311ContextProviderProps } from './Libre311Context';
	export let props: Libre311ContextProviderProps;

	const libre311Context = createLibre311Context(props);
</script>

<svelte:head>
	<script src="https://www.google.com/recaptcha/enterprise.js?render={props.recaptchaKey}"></script>
</svelte:head>

<slot {libre311Context} />
