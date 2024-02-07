<script lang="ts">
	import Bars3 from '$lib/components/Svg/Bars3.svelte';
	import SplashLoading from '$lib/components/SplashLoading.svelte';
	import Libre311ContextProvider from '$lib/context/Libre311ContextProvider.svelte';
	import { type Libre311ContextProviderProps } from '$lib/context/Libre311Context';
	import '../app.pcss';
	import '../../node_modules/leaflet-geosearch/dist/geosearch.css';

	import {
		asAsyncFailure,
		asAsyncSuccess,
		ASYNC_IN_PROGRESS,
		type AsyncResult
	} from '$lib/services/http';
	import { libre311Factory, type Libre311ServiceProps } from '$lib/services/Libre311/Libre311';
	import { getModeFromEnv, type Mode } from '$lib/services/mode';
	import { recaptchaServiceFactory } from '$lib/services/RecaptchaService';

	const mode: Mode = getModeFromEnv(import.meta.env);
	const recaptchaKey = String(import.meta.env.VITE_GOOGLE_RECAPTCHA_KEY);

	let contextProviderProps: AsyncResult<Libre311ContextProviderProps> = ASYNC_IN_PROGRESS;

	const synchronousContextProviderProps: Omit<Libre311ContextProviderProps, 'service'> = {
		recaptchaKey,
		mode
	};

	async function initLibre311ContextProps(serviceProps: Libre311ServiceProps) {
		try {
			const service = await libre311Factory(serviceProps);
			contextProviderProps = asAsyncSuccess({ ...synchronousContextProviderProps, service });
		} catch (error) {
			contextProviderProps = asAsyncFailure(error);
		}
	}

	initLibre311ContextProps({
		baseURL: import.meta.env.VITE_BACKEND_URL,
		recaptchaService: recaptchaServiceFactory(mode, { recaptchaKey })
	});
</script>

{#if contextProviderProps.type == 'success'}
	<Libre311ContextProvider props={contextProviderProps.value} let:libre311Context>
		<header>
			<h1>{libre311Context.service.getJurisdictionConfig().jurisdiction_name}</h1>
			<div class="controls">
				<Bars3 />
			</div>
		</header>
		<main>
			<slot />
		</main>
	</Libre311ContextProvider>
{:else if contextProviderProps.type == 'inProgress'}
	<SplashLoading />
{:else}
	<!-- todo nice looking component for error-->
	<h1>something went wrong</h1>
{/if}

<style>
	:global(:root) {
		--header-height: 4rem;
	}
	header {
		height: var(--header-height);
		background-color: hsl(var(--primary));
		color: hsl(var(--primary-content));
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 0 1rem;
	}
	.controls {
		display: flex;
		gap: 1rem;
	}

	main {
		height: calc(100dvh - var(--header-height));
	}
</style>
