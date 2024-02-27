<script lang="ts">
	import SplashLoading from '$lib/components/SplashLoading.svelte';
	import Bars3 from '$lib/components/Svg/Bars3.svelte';
	import { type Libre311ContextProviderProps } from '$lib/context/Libre311Context';
	import Libre311ContextProvider from '$lib/context/Libre311ContextProvider.svelte';
	import SomethingWentWrong from '$lib/components/SomethingWentWrong.svelte';
	import '../../node_modules/leaflet-geosearch/dist/geosearch.css';
	import '../app.pcss';

	import MenuDrawer from '$lib/components/MenuDrawer.svelte';

	import {
		asAsyncFailure,
		asAsyncSuccess,
		ASYNC_IN_PROGRESS,
		type AsyncResult
	} from '$lib/services/http';
	import { getJurisdictionConfig } from '$lib/services/Libre311/Libre311';
	import { getModeFromEnv, type Mode } from '$lib/services/mode';
	import { loadRecaptchaProps } from '$lib/services/RecaptchaService';
	import User from '$lib/components/User.svelte';

	let contextProviderProps: AsyncResult<Libre311ContextProviderProps> = ASYNC_IN_PROGRESS;

	let open: boolean = false;

	function closeDrawer() {
		open = false;
	}

	async function initLibre311ContextProps() {
		try {
			const mode: Mode = getModeFromEnv(import.meta.env);
			const libreBaseURL = String(import.meta.env.VITE_BACKEND_URL) ?? '/api';

			const [recaptchaServiceProps, jurisdictionConfig] = await Promise.all([
				loadRecaptchaProps(mode),
				getJurisdictionConfig(libreBaseURL)
			]);

			const ctxProps: Libre311ContextProviderProps = {
				mode: mode,
				libreServiceProps: {
					baseURL: libreBaseURL,
					jurisdictionConfig
				},
				recaptchaServiceProps,
				unityAuthServiceProps: { baseURL: String(import.meta.env.VITE_AUTH_URL) }
			};

			contextProviderProps = asAsyncSuccess(ctxProps);
		} catch (error) {
			console.error(error);
			contextProviderProps = asAsyncFailure(error);
		}
	}

	initLibre311ContextProps();
</script>

{#if contextProviderProps.type == 'success'}
	<Libre311ContextProvider props={contextProviderProps.value} let:libre311Context>
		<header class="flex items-center justify-center">
			<div class="flex gap-4">
				<button
					type="button"
					on:click={() => {
						open = !open;
					}}
				>
					<Bars3 />
				</button>
				<h1>{libre311Context.service.getJurisdictionConfig().name}</h1>
			</div>

			<User />
		</header>
		<main>
			<MenuDrawer {open} handleClose={closeDrawer} />
			<slot />
		</main>
	</Libre311ContextProvider>
{:else if contextProviderProps.type == 'inProgress'}
	<SplashLoading />
{:else}
	<SomethingWentWrong />
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
	main {
		height: calc(100dvh - var(--header-height));
	}
</style>
