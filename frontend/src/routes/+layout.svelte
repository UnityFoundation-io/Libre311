<script lang="ts">
	import SplashLoading from '$lib/components/SplashLoading.svelte';
	import Bars3 from '$lib/components/Svg/Bars3.svelte';
	import { type Libre311ContextProviderProps } from '$lib/context/Libre311Context';
	import Libre311ContextProvider from '$lib/context/Libre311ContextProvider.svelte';
	import { createJurisdictionContext } from '$lib/context/JurisdictionContext';
	import SomethingWentWrong from '$lib/components/SomethingWentWrong.svelte';
	import '../../node_modules/leaflet-geosearch/dist/geosearch.css';
	import '../app.pcss';

	import MenuDrawer from '$lib/components/MenuDrawer.svelte';

	import {
		asAsyncFailure,
		asAsyncSuccess,
		ASYNC_IN_PROGRESS,
		type AsyncResult,
		type AsyncSuccess
	} from '$lib/services/http';
	import { onDestroy } from 'svelte';
	import { getJurisdictionConfig, type JurisdictionConfig } from '$lib/services/Libre311/Libre311';
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
			const libreBaseURL = String(import.meta.env.VITE_BACKEND_URL ?? '') || '/api';

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
				unityAuthServiceProps: { baseURL: jurisdictionConfig.auth_base_url }
			};

			contextProviderProps = asAsyncSuccess(ctxProps);
		} catch (error) {
			console.error(error);
			contextProviderProps = asAsyncFailure(error);
		}
	}

	initLibre311ContextProps();

	let fullH1El: HTMLElement;
	let observer: ResizeObserver;
	let useAbbrev = false;

	function trySetupH1Resize() {
		if (fullH1El && !observer) {
			observer = new ResizeObserver(() => {
				useAbbrev = fullH1El.clientHeight > parseFloat(getComputedStyle(fullH1El).lineHeight) + 1;
			});
			observer.observe(fullH1El);
		}
	}

	// Cleanup
	onDestroy(() => {
		if (observer && fullH1El) observer.disconnect();
	});

	let jurisdictionConfig: JurisdictionConfig;

	$: {
		let cppType = contextProviderProps.type;
		if (cppType == 'success') {
			let contextProviderPropsSuccess =
				contextProviderProps as AsyncSuccess<Libre311ContextProviderProps>;
			let primaryColor =
				contextProviderPropsSuccess.value.libreServiceProps.jurisdictionConfig.primary_color;
			if (primaryColor) {
				document.documentElement.style.setProperty('--primary', primaryColor);
			}
			let primaryHoverColor =
				contextProviderPropsSuccess.value.libreServiceProps.jurisdictionConfig.primary_hover_color;
			if (primaryHoverColor) {
				document.documentElement.style.setProperty('--hover', primaryHoverColor);
			}
			jurisdictionConfig = contextProviderPropsSuccess.value.libreServiceProps.jurisdictionConfig;
		}
	}

	$: if (fullH1El) trySetupH1Resize();
</script>

{#if contextProviderProps.type === 'success'}
	<Libre311ContextProvider props={contextProviderProps.value}>
		<!-- eslint-disable-next-line @typescript-eslint/no-unused-vars -->
		{@const jurisdictionContext = createJurisdictionContext()}
		<header class="flex items-center justify-center">
			<div class="mb-2 flex w-full gap-4">
				<button
					aria-label="navigation"
					type="button"
					class="rounded-md focus:outline-none focus:ring-2 focus:ring-white"
					on:click={() => {
						open = !open;
					}}
				>
					<Bars3 />
				</button>
				<div class="relative w-full">
					<!-- Visible heading (controls layout height) -->
					<h1>
						{useAbbrev && jurisdictionConfig.abbreviated_name
							? jurisdictionConfig.abbreviated_name
							: jurisdictionConfig.name}
					</h1>
					<!-- Hidden measurement element to determine when to wrap -->
					<h1 bind:this={fullH1El} class="absolute left-0 top-0 w-full whitespace-normal opacity-0">
						{jurisdictionConfig.name}
					</h1>
				</div>
			</div>

			<User />
		</header>
		<main>
			<MenuDrawer {open} handleClose={closeDrawer} />
			<section id="slot">
				<slot />
			</section>
		</main>
	</Libre311ContextProvider>
{:else if contextProviderProps.type === 'inProgress'}
	<SplashLoading />
{:else}
	<SomethingWentWrong />
{/if}

<style>
	:global(:root) {
		--header-height: 3.2rem;
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
		display: flex;
		height: calc(100dvh - var(--header-height));
		width: 100%;
		overflow-y: auto; /* slot scrolls */
		overflow-x: hidden; /* defensive */
	}
	section#slot {
		flex: 1 1 auto; /* fill remaining width */
		width: 100vw; /* ensure full-width fill */
		min-width: 0; /* CRITICAL: prevents zoom overflow */
	}
	h1 {
		font-size: clamp(1.5rem, -0.875rem + 8.333333vw, 2.5rem);
	}
</style>
