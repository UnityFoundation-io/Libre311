// Copyright 2023 Libre311 Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
