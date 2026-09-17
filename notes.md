# Notes

## `pcServerName` vs `serverAndPort` (pctestrun)

These two fields sound similar but point to completely different servers. In this
codebase the fields are named `pcServerName` and `serverAndPort` (there is no
field literally named `lreServerAndPort`).

### `pcServerName` — the LRE/EPE server address (user-entered)

The LoadRunner Enterprise (EPE) server that the plugin connects to and runs tests on.

- **UI field:** `pc.pcServerName`, a visible textbox titled "LRE Server"
  (`PcTestRunBuilder/config.jelly`).
- **Set by:** the user.
- **Purpose:** identifies the actual LRE server (optionally with tenant) used to
  build the REST API calls.
- **Runtime use:** passed into the REST client —
  `restProxy = new PcRestProxy(model.isHTTPSProtocol(), model.getPcServerName(true), ...)`
  (`PcTestRunClient.java`).
- **Parametrizable:** yes. `getPcServerName(true)` runs the value through
  `useParameterIfNeeded`, so it can be a build parameter such as `${LRE_SERVER}`.
- **Validated:** yes. `doCheckPcServerName` requires a non-empty value ("LRE Server").
- **Format flexibility:** the client-side `getLreServer()` JavaScript strips schemes
  (`http://`, `https://`) and path suffixes (`/lre`, `/loadtest`, `/admin`, `/login`,
  etc.) and can split off a tenant, so the user may paste a full URL and it is
  normalized to server + tenant.

### `serverAndPort` — the Jenkins base URL (auto-derived)

The Jenkins instance base URL. Not the LRE server.

- **UI field:** `pc.serverAndPort`, inside an `<f:invisibleEntry>` — hidden from the user
  (`PcTestRunBuilder/config.jelly`).
- **Set by:** JavaScript (`assignhostPath()`), derived from `window.location.href`:
  the Jenkins page URL is split on `/job` and the base is kept
  (e.g. `https://my-jenkins:8080`).
- **Purpose:** used to build the clickable report links in the build log — the
  "View analysis report" / "Download report" hyperlinks that point back into Jenkins:
  `getPcTestRunModel().getServerAndPort() + "/" + build.getUrl() + viewUrl`
  (`PcTestRunBuilder.java`).
- **Parametrizable:** no.
- **Validated:** no. It is plumbing, filled in automatically.

### Summary

| | `pcServerName` | `serverAndPort` |
|---|---|---|
| Represents | The **LRE/EPE server** to run tests on | The **Jenkins** base URL |
| UI | Visible textbox ("LRE Server") | Hidden, auto-filled from browser URL |
| Set by | User input | JavaScript from `window.location` |
| Used for | Building REST API calls to LRE | Building report hyperlinks back to Jenkins |
| Parametrizable | Yes (`${...}` build params) | No |

Despite the similar-sounding names, `pcServerName` is the *target* LRE server being
driven, while `serverAndPort` is the *host* Jenkins instance, captured only so the
plugin can render working links to the published reports.
