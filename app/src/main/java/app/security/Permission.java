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

package app.security;

public enum Permission {
    LIBRE311_ADMIN_EDIT_SYSTEM("LIBRE311_ADMIN_EDIT-SYSTEM"),
    LIBRE311_ADMIN_VIEW_SYSTEM("LIBRE311_ADMIN_VIEW-SYSTEM"),
    LIBRE311_ADMIN_EDIT_TENANT("LIBRE311_ADMIN_EDIT-TENANT"),
    LIBRE311_ADMIN_VIEW_TENANT("LIBRE311_ADMIN_VIEW-TENANT"),
    LIBRE311_ADMIN_EDIT_SUBTENANT("LIBRE311_ADMIN_EDIT-SUBTENANT"),
    LIBRE311_ADMIN_VIEW_SUBTENANT("LIBRE311_ADMIN_VIEW-SUBTENANT"),
    LIBRE311_REQUEST_EDIT_SYSTEM("LIBRE311_REQUEST_EDIT-SYSTEM"),
    LIBRE311_REQUEST_VIEW_SYSTEM("LIBRE311_REQUEST_VIEW-SYSTEM"),
    LIBRE311_REQUEST_EDIT_TENANT("LIBRE311_REQUEST_EDIT-TENANT"),
    LIBRE311_REQUEST_VIEW_TENANT("LIBRE311_REQUEST_VIEW-TENANT"),
    LIBRE311_REQUEST_EDIT_SUBTENANT("LIBRE311_REQUEST_EDIT-SUBTENANT"),
    LIBRE311_REQUEST_VIEW_SUBTENANT("LIBRE311_REQUEST_VIEW-SUBTENANT");

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}