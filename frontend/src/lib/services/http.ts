// https://codewithstyle.info/typescript-async-result/#:~:text=AsyncResult%20is%20a%20simple%20type,fetch%20call%20in%20our%20case).
export type AsyncResult<TResult, TError = unknown> =
	| AsyncInProgress
	| AsyncSuccess<TResult>
	| AsyncFailure<TError>;

type AsyncInProgress = {
	type: 'inProgress';
};

export type AsyncSuccess<TResult> = {
	type: 'success';
	value: TResult;
};

type AsyncFailure<TError> = {
	type: 'failure';
	error: TError;
};

export const asAsyncSuccess = <TResult>(value: TResult): AsyncSuccess<TResult> => ({
	type: 'success',
	value
});

export const asAsyncFailure = <TError>(error: TError): AsyncFailure<TError> => ({
	type: 'failure',
	error
});

export const ASYNC_IN_PROGRESS: AsyncInProgress = { type: 'inProgress' };
