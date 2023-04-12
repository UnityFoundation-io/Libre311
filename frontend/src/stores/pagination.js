import { writable } from 'svelte/store';

const pagination = writable({
    totalSize: 0,
    totalPages: 0,
    currentPage: 0,
    itemsPerPage: 0
});

export const totalSize = writable(pagination.totalSize);
export const totalPages = writable(pagination.totalPages);
export const currentPage = writable(pagination.currentPage);
export const itemsPerPage = writable(pagination.itemsPerPage);